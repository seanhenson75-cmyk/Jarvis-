import {KokoroTTS} from 'kokoro-js';
import {env} from '@huggingface/transformers';
env.allowRemoteModels=false;env.allowLocalModels=true;env.localModelPath='/models/';
env.backends.onnx.wasm.wasmPaths='/assets/';env.backends.onnx.wasm.numThreads=1;env.backends.onnx.wasm.proxy=false;
let modelPromise,latest=0,queue=Promise.resolve();
function load(){return modelPromise??=KokoroTTS.from_pretrained('kokoro',{dtype:'q8',device:'wasm',progress_callback:p=>{if(p.status==='progress')self.postMessage({type:'progress',progress:Math.round(p.progress||0),file:p.file});}}).catch(e=>{modelPromise=null;throw e;});}
self.onmessage=({data})=>{if(data.type==='cancel'){latest=data.id;return;}if(data.type==='load'){load().then(()=>self.postMessage({type:'ready'})).catch(e=>self.postMessage({type:'error',message:e.message}));return;}if(data.type!=='speak')return;latest=data.id;const id=data.id;queue=queue.catch(()=>{}).then(async()=>{try{const tts=await load();for(const chunk of data.chunks){if(latest!==id)break;const audio=await tts.generate(chunk,{voice:'bm_george',speed:0.94});if(latest!==id)break;const pcm=audio.audio;self.postMessage({type:'audio',id,pcm,rate:audio.sampling_rate},[pcm.buffer]);}if(latest===id)self.postMessage({type:'done',id});}catch(e){self.postMessage({type:'error',id,message:e.message});}});};
