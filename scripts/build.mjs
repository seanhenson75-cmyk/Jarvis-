import {build} from 'esbuild';
import {mkdir,copyFile,readFile,writeFile,access} from 'node:fs/promises';
import {createHash} from 'node:crypto';
const assets='mobile/assets';await mkdir(assets,{recursive:true});
await build({entryPoints:['src/app.js','src/speech-worker.js'],outdir:assets,bundle:true,format:'esm',platform:'browser',target:'es2022',minify:true,plugins:[{name:'local-voice',setup(b){b.onLoad({filter:/kokoro-js\/dist\/kokoro\.js$/},async({path})=>({contents:(await readFile(path,'utf8')).replace('https://huggingface.co/onnx-community/Kokoro-82M-v1.0-ONNX/resolve/main/voices/','/models/voices/'),loader:'js'}));}}]});
for(const file of ['ort-wasm-simd-threaded.mjs','ort-wasm-simd-threaded.wasm','ort-wasm-simd-threaded.jsep.mjs','ort-wasm-simd-threaded.jsep.wasm'])await copyFile('node_modules/onnxruntime-web/dist/'+file,assets+'/'+file);
const revision='1939ad2a8e416c0acfeecc08a694d14ef25f2231';
const files=['config.json','tokenizer.json','tokenizer_config.json','onnx/model_quantized.onnx','voices/bm_george.bin'];
const hashes={};
for(const file of files){const local='mobile/models/'+(file.startsWith('voices/')?file:'kokoro/'+file);await mkdir(local.slice(0,local.lastIndexOf('/')),{recursive:true});try{await access(local);}catch{console.log('Downloading pinned voice asset:',file);const r=await fetch(`https://huggingface.co/onnx-community/Kokoro-82M-v1.0-ONNX/resolve/${revision}/${file}`,{signal:AbortSignal.timeout(180000)});if(!r.ok)throw Error(`Voice asset ${file}: HTTP ${r.status}`);await writeFile(local,new Uint8Array(await r.arrayBuffer()));}hashes[file]=createHash('sha256').update(await readFile(local)).digest('hex');}
await writeFile(assets+'/voice-manifest.json',JSON.stringify({model:'Kokoro-82M-v1.0',revision,voice:'bm_george',hashes}));
await copyFile('node_modules/kokoro-js/LICENSE',assets+'/KOKORO-LICENSE.txt');
console.log('Built JARVIS with local British male speech.');
