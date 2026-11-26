package com.henson.jarvis.phone;
import java.net.UnknownHostException;
public final class DnsRetry {
 public interface Task<T>{T run()throws Exception;}
 public interface Pause{void waitFor(long ms)throws InterruptedException;}
 private DnsRetry(){}
 public static <T>T run(Task<T> task)throws Exception{return run(task,Thread::sleep);}
 static <T>T run(Task<T> task,Pause pause)throws Exception{for(int attempt=0;;attempt++){try{return task.run();}catch(UnknownHostException e){if(attempt==2)throw e;pause.waitFor(attempt==0?1500:10000);}}}
}
