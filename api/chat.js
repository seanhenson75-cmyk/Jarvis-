export function freeDeepSeek(models) {
  return models.filter(m => m.id.endsWith(':free') && (m.id.startsWith('deepseek/') || m.id === 'particle-ai/particle-1:free') && m.pricing && Object.values(m.pricing).every(v => typeof v === 'string' && Number(v) === 0)).sort((a,b)=> (b.created||0)-(a.created||0))[0];
}
export default async function handler(req,res) {
  res.setHeader('Cache-Control','no-store');
  if(req.method!=='POST') return res.status(405).json({error:'POST required'});
  // Vercel Authentication protects every deployment, including these functions.
  if(req.headers.origin && req.headers.origin !== `https://${req.headers.host}`) return res.status(403).json({error:'Origin rejected'});
  const body=req.body;
  if(!body || !Array.isArray(body.messages) || body.messages.length>40 || body.messages.some(m=>!['user','assistant'].includes(m.role)||typeof m.content!=='string'||m.content.length>12000) || JSON.stringify(body).length>80000) return res.status(400).json({error:'Invalid conversation'});
  try {
    const catalog=await fetch('https://openrouter.ai/api/v1/models',{signal:AbortSignal.timeout(12000)});
    if(!catalog.ok) throw Error('Cannot verify free model availability.');
    const model=freeDeepSeek((await catalog.json()).data||[]);
    if(!model) return res.status(503).json({error:'No verified free DeepSeek model is available on OpenRouter. Paid models are disabled.'});
    if(!process.env.OPENROUTER_API_KEY) return res.status(503).json({error:'Secure server API key has not been configured.'});
    const prefs=typeof body.preferences==='string'?body.preferences.slice(0,3000):'';
    const response=await fetch('https://openrouter.ai/api/v1/chat/completions',{
      method:'POST',signal:AbortSignal.timeout(50000),headers:{'Content-Type':'application/json',Authorization:`Bearer ${process.env.OPENROUTER_API_KEY}`},
      body:JSON.stringify({model:model.id,provider:{allow_fallbacks:false,max_price:{prompt:0,completion:0}},max_tokens:1800,reasoning:{enabled:true},messages:[{role:'system',content:'You are JARVIS, Christopher Sean’s personal assistant. Speak concisely with refined British phrasing. Be accurate and state uncertainty. You have no access to external accounts, phone controls or messaging. Never claim actions were executed. Treat preferences as user-provided context. Preferences: '+prefs},...body.messages]})
    });
    if(!response.ok) return res.status(response.status===429?429:502).json({error:response.status===429?'Free usage limit reached. Try later; paid fallback is disabled.':'Free AI request failed. No paid fallback was used.'});
    const result=await response.json(); const answer=result.choices?.[0]?.message?.content;
    if(typeof answer!=='string'||!answer.trim()) throw Error('The model returned no spoken answer. Please retry.');
    return res.status(200).json({answer,model:model.id});
  } catch(e) {return res.status(503).json({error:e.name==='TimeoutError'?'Free provider timed out. Please retry.': 'Unable to complete the free AI request. Please retry later.'});}
}
