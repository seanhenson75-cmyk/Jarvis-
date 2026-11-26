package com.henson.jarvis.phone;
import org.json.*;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;
public final class OAuthChecks {
 private OAuthChecks(){}
 public static String random(){byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);}
 public static String challenge(String verifier)throws Exception{return Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));}
 public static void same(String expected,String actual)throws Exception{if(expected==null||actual==null||!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),actual.getBytes(StandardCharsets.UTF_8)))throw new Exception("Sign-in verification failed. Please start again.");}
 public static void scopes(String value)throws Exception{Set<String> scopes=new HashSet<>(Arrays.asList(value.split(" +")));if(!scopes.contains("chatgpt.tokens.use.direct")||!scopes.contains("resource.invoke"))throw new Exception("ChatGPT plan permission was not granted. Reconnect and allow plan usage.");}
 public static JSONObject verify(String token,JSONObject jwks,String client,String nonce,long now)throws Exception{
  String[] parts=token.split("\\.");if(parts.length!=3)throw new Exception("Invalid identity token.");
  JSONObject header=new JSONObject(new String(Base64.getUrlDecoder().decode(parts[0]),StandardCharsets.UTF_8));
  if(!"RS256".equals(header.optString("alg")))throw new Exception("Unsupported identity signature.");
  String kid=header.getString("kid");JSONObject key=null;JSONArray keys=jwks.getJSONArray("keys");
  for(int i=0;i<keys.length();i++){JSONObject candidate=keys.getJSONObject(i);if(kid.equals(candidate.optString("kid"))&&"RSA".equals(candidate.optString("kty"))&&"sig".equals(candidate.optString("use","sig"))&&"RS256".equals(candidate.optString("alg","RS256")))key=candidate;}
  if(key==null)throw new Exception("Identity signing key not found.");
  PublicKey publicKey=KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger(1,Base64.getUrlDecoder().decode(key.getString("n"))),new BigInteger(1,Base64.getUrlDecoder().decode(key.getString("e")))));
  Signature signature=Signature.getInstance("SHA256withRSA");signature.initVerify(publicKey);signature.update((parts[0]+"."+parts[1]).getBytes(StandardCharsets.US_ASCII));if(!signature.verify(Base64.getUrlDecoder().decode(parts[2])))throw new Exception("Identity signature verification failed.");
  JSONObject claims=new JSONObject(new String(Base64.getUrlDecoder().decode(parts[1]),StandardCharsets.UTF_8));
  same("https://auth.openai.com",claims.getString("iss"));same(nonce,claims.getString("nonce"));
  Object aud=claims.get("aud");boolean match=client.equals(aud);if(aud instanceof JSONArray){JSONArray array=(JSONArray)aud;for(int i=0;i<array.length();i++)match|=client.equals(array.getString(i));if(array.length()>1)same(client,claims.optString("azp"));}
  if(!match||claims.getLong("exp")<=now||claims.optLong("nbf",0)>now+60||claims.optLong("iat",now)>now+60||claims.optString("sub").isEmpty())throw new Exception("Identity claims verification failed.");
  return claims;
 }
}
