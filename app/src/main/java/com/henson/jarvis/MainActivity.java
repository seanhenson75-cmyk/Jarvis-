package com.henson.jarvis;
import android.app.*;import android.os.*;import android.content.*;import android.net.*;import android.speech.RecognizerIntent;import android.speech.tts.*;import android.widget.*;import android.graphics.Color;import java.util.*;
public class MainActivity extends Activity {
 TextToSpeech tts; TextView status; EditText command; boolean ready;
 public void onCreate(Bundle b){super.onCreate(b); LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(32,64,32,32);l.setBackgroundColor(0xff101820);setContentView(l);
 TextView title=new TextView(this);title.setText("J A R V I S");title.setTextSize(32);title.setTextColor(0xff63dfff);l.addView(title);
 status=new TextView(this);status.setTextColor(Color.WHITE);status.setTextSize(18);status.setText("Systems ready. Tap to speak.");l.addView(status);
 command=new EditText(this);command.setHint("Enter command");command.setTextColor(Color.WHITE);l.addView(command);
 Button run=new Button(this);run.setText("Execute");l.addView(run);run.setOnClickListener(v->execute(command.getText().toString()));
 Button speak=new Button(this);speak.setText("Speak to JARVIS");l.addView(speak);speak.setOnClickListener(v->{try{Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-GB");startActivityForResult(i,1);}catch(Exception e){say("Speech recognition is unavailable. Please type your command.");}});
 Button wifi=new Button(this);wifi.setText("Diagnose Wi-Fi");l.addView(wifi);wifi.setOnClickListener(v->diagnose());
 tts=new TextToSpeech(this,s->{if(s==TextToSpeech.SUCCESS){tts.setLanguage(Locale.UK);tts.setPitch(.85f);tts.setSpeechRate(.92f);ready=true;say("Systems online, sir.");}});}
 void say(String s){status.setText(s);if(ready)tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"jarvis");}
 void execute(String s){s=s.toLowerCase(Locale.ROOT);if(s.contains("wifi")||s.contains("wi-fi")||s.contains("network"))diagnose();else say("That command is not implemented yet, sir.");}
 void diagnose(){ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);Network n=cm.getActiveNetwork();NetworkCapabilities c=cm.getNetworkCapabilities(n);if(c==null||!c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){say("No active Wi-Fi connection detected, sir.");return;}LinkProperties p=cm.getLinkProperties(n);String gateway="unknown";if(p!=null)for(RouteInfo r:p.getRoutes())if(r.isDefaultRoute()&&r.getGateway()!=null){gateway=r.getGateway().getHostAddress();break;}say("Wi-Fi connected, sir. Estimated downstream bandwidth: "+c.getLinkDownstreamBandwidthKbps()/1000+" megabits per second. Gateway: "+gateway+". Internet validation: "+(c.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)?"successful":"not confirmed")+". No settings changed.");}
 protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==1&&c==RESULT_OK&&d!=null){ArrayList<String>a=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(a!=null&&!a.isEmpty()){command.setText(a.get(0));execute(a.get(0));}}}
 protected void onDestroy(){if(tts!=null)tts.shutdown();super.onDestroy();}
}