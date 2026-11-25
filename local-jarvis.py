import json, urllib.request, tkinter as tk, threading, subprocess, os
from tkinter.scrolledtext import ScrolledText
MODEL=os.environ.get("JARVIS_MODEL","deepseek-r1:1.5b")
history=[{"role":"system","content":"You are JARVIS, a concise professional personal assistant. Never claim to execute actions you cannot execute. Require approval for sending emails or sensitive actions."}]
root=tk.Tk();root.title("JARVIS | Local AI");root.geometry("700x600")
out=ScrolledText(root,wrap="word",bg="#081b28",fg="#8ce2ff");out.pack(fill="both",expand=True)
entry=tk.Entry(root);entry.pack(fill="x")
def speak(s):
    env=dict(os.environ,JARVIS_SPEECH=s)
    script="Add-Type -AssemblyName System.Speech; $s=New-Object System.Speech.Synthesis.SpeechSynthesizer; $v=$s.GetInstalledVoices() | Where-Object {$_.VoiceInfo.Gender -eq 'Male' -and $_.VoiceInfo.Culture.Name -eq 'en-GB'} | Select-Object -First 1; if (!$v) {$v=$s.GetInstalledVoices() | Where-Object {$_.VoiceInfo.Gender -eq 'Male'} | Select-Object -First 1}; if ($v) {$s.SelectVoice($v.VoiceInfo.Name); $s.Rate=-2; $s.Speak($env:JARVIS_SPEECH)}"
    if os.name=="nt": subprocess.run(["powershell","-NoProfile","-Command",script],env=env)
def show(s): out.insert("end",s+"\n\n");out.see("end")
def send(event=None):
    q=entry.get().strip()
    if not q:return
    entry.delete(0,"end");entry.config(state="disabled");show("YOU: "+q)
    def work():
        try:
            messages=history+[{"role":"user","content":q}]
            req=urllib.request.Request("http://127.0.0.1:11434/api/chat",data=json.dumps({"model":MODEL,"messages":messages,"stream":False}).encode(),headers={"Content-Type":"application/json"})
            with urllib.request.urlopen(req,timeout=300) as r: a=json.load(r)["message"]["content"]
            history.extend([messages[-1],{"role":"assistant","content":a}])
            root.after(0,lambda:show("JARVIS: "+a));threading.Thread(target=speak,args=(a,),daemon=True).start()
        except Exception as e:
            msg=str(e);root.after(0,lambda:show("Local AI unavailable: "+msg))
        finally:root.after(0,lambda:entry.config(state="normal"))
    threading.Thread(target=work,daemon=True).start()
entry.bind("<Return>",send);tk.Button(root,text="Ask JARVIS",command=send).pack()
show("JARVIS ready. Start Ollama and install "+MODEL+" first. Male speech requires an installed Windows male voice.")
root.mainloop()
