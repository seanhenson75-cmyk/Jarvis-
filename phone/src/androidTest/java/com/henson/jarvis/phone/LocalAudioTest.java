package com.henson.jarvis.phone;
import androidx.test.ext.junit.runners.AndroidJUnit4;import androidx.test.platform.app.InstrumentationRegistry;import org.junit.*;import org.junit.runner.RunWith;import static org.junit.Assert.*;import java.net.*;import java.io.*;
@RunWith(AndroidJUnit4.class)
public class LocalAudioTest {
 @Test public void generatedAudioIsSingleUseAndNotPubliclyListed()throws Exception{try(LocalSite site=new LocalSite(InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets(),uri->{})){byte[] data={1,2,3,4};String path=site.addAudio(data);HttpURLConnection first=(HttpURLConnection)new URL(site.origin()+path).openConnection();assertEquals(200,first.getResponseCode());try(InputStream in=first.getInputStream()){for(byte value:data)assertEquals(value,in.read());assertEquals(-1,in.read());}first.disconnect();HttpURLConnection second=(HttpURLConnection)new URL(site.origin()+path).openConnection();assertEquals(404,second.getResponseCode());second.disconnect();}}
}
