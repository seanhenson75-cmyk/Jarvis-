package com.henson.jarvis.phone;
import android.content.*;import android.webkit.WebView;import android.view.*;import androidx.test.ext.junit.runners.AndroidJUnit4;import androidx.test.platform.app.InstrumentationRegistry;import org.junit.*;import org.junit.runner.RunWith;import static org.junit.Assert.*;import java.util.concurrent.*;import java.util.concurrent.atomic.AtomicReference;
@RunWith(AndroidJUnit4.class)
public class LocalInterfaceTest {
 private MainActivity activity;private WebView web;
 @Before public void launch()throws Exception{android.app.Instrumentation i=InstrumentationRegistry.getInstrumentation();activity=(MainActivity)i.startActivitySync(new Intent(i.getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));i.runOnMainSync(()->web=(WebView)((ViewGroup)activity.findViewById(android.R.id.content)).getChildAt(0));await("document.getElementById('native-account')?.hidden===false",30000);}
 @After public void close(){if(activity!=null)InstrumentationRegistry.getInstrumentation().runOnMainSync(()->activity.finish());}
 private String evaluate(String code)throws Exception{AtomicReference<String> value=new AtomicReference<>();CountDownLatch done=new CountDownLatch(1);InstrumentationRegistry.getInstrumentation().runOnMainSync(()->web.evaluateJavascript(code,result->{value.set(result);done.countDown();}));assertTrue("JavaScript evaluation did not finish",done.await(5,TimeUnit.SECONDS));return value.get();}
 private void await(String condition,int ms)throws Exception{long until=System.currentTimeMillis()+ms;while(System.currentTimeMillis()<until){if("true".equals(evaluate(condition)))return;Thread.sleep(500);}fail("Local UI condition failed: "+condition+". Status: "+evaluate("document.getElementById('status')?.textContent"));}
 @Test public void connectsToOpenAILoginServerWhileForeground()throws Exception{new OpenAIClient(new TokenVault(InstrumentationRegistry.getInstrumentation().getTargetContext())).checkLoginServer();}
 @Test public void presentsLocalChatGPTAccountConnection()throws Exception{assertEquals("true",evaluate("location.hostname==='127.0.0.1'&&!!window.JarvisNative&&document.getElementById('connect-chatgpt').textContent==='Continue with ChatGPT'"));}
 @Test public void generatesBritishMaleAudioInsideAndroidWebView()throws Exception{evaluate("window.__audioCount=0;window.__startOriginal=AudioBufferSourceNode.prototype.start;AudioBufferSourceNode.prototype.start=function(...args){if(this.buffer&&this.buffer.length>2400)window.__audioCount++;return window.__startOriginal.apply(this,args)};document.getElementById('preview').click();true");await("window.__audioCount>0",120000);}

 @Test public void nativeStartAvoidsBrokenWebViewCapture()throws Exception{
  android.app.Instrumentation i=InstrumentationRegistry.getInstrumentation();i.getUiAutomation().grantRuntimePermission(i.getTargetContext().getPackageName(),android.Manifest.permission.RECORD_AUDIO);
  evaluate("window.__legacyMicCalls=0;if(navigator.mediaDevices)navigator.mediaDevices.getUserMedia=()=>{window.__legacyMicCalls++;return Promise.reject(new Error('WebView audio source fixture failure'))};window.__realNativeResult=window.jarvisNativeResult;window.jarvisNativeResult=(id,data)=>{if(Object.hasOwn(data,'connected')){data.connected=true;data.email='fixture@example.invalid'}window.__realNativeResult(id,data)};window.dispatchEvent(new Event('jarvis-account-changed'));true");
  await("document.getElementById('connect-chatgpt').textContent==='ChatGPT account'",10000);
  evaluate("window.SpeechRecognition=class{start(){this.onstart?.()}abort(){this.onend?.()}};document.getElementById('start').click();true");
  await("document.getElementById('phase').textContent==='LISTENING'",120000);
  assertEquals("0",evaluate("window.__legacyMicCalls"));
  assertFalse(evaluate("document.getElementById('status').textContent").contains("Could not start audio source"));
  evaluate("document.getElementById('stop').click();true");
 }
}
