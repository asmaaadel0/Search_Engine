package crawler;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.SortedMap;
import java.util.TreeMap;

public class UrlNormalizer {
    public static String getNormalizedURL(String s) {
        try {
            URL url = new URL(s);
            String scheme = (url.getProtocol()+"://").toLowerCase();
            String host = getHostName(url.getHost());
            String port = getPort(url.getPort());
            String path = normalizePath(url.getPath());
            String query = sortQueryParamerters(url.getQuery());
            return scheme+host+port+path+query;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static String normalizePath(String path) {
        String p = 	path.replaceAll("//", "/").replace("index.html", "")
                .replace("index.htm", "")
                .replace("index.php", "")
                .replaceAll("\\{", "%7B")
                .replaceAll("\\}", "%7D");
        if(p.endsWith("/")) {
            p = p.substring(0, p.length()-1);
        }
        return p;
    }

    public static String getHostName(String host) {
        //check if IP not domain name
        if(host.matches("([0-9]+[.]*)+")){
            try {
                host = InetAddress.getByName(host).getHostName();
            } catch (UnknownHostException e) {
                //e.printStackTrace();
            }
        }
        host = host.toLowerCase();
        // remove www
        if(host.length() > 3 && host.startsWith("www")) {
            host = host.substring(host.indexOf('.')+1);
        }
        return host;
    }

    public static String getPort(int port) {
        return (port == 80 || port == -1) ? "" : (":" + String.valueOf(port));
    }

    public static String sortQueryParamerters(String query) {
        if(query == null || query.equals("")) return "";
        String[] parameters = query.split("&");
        SortedMap<String, String> sortedPar = new TreeMap<String, String>();
        for(String s : parameters) {
            int idxSep = s.indexOf('=');
            if(idxSep == -1) continue;
            String par = s.substring(0, idxSep);
            String val = s.substring(idxSep+1);
            sortedPar.put(par, val);
        }
        String sortedQuery = "";
        for (SortedMap.Entry<String,String> p : sortedPar.entrySet()) {
            if(p.getValue().equals("")) continue;
            if(!sortedQuery.equals("")) sortedQuery+="&";
            sortedQuery += p.getKey()+"="+p.getValue();
        }
        return !sortedQuery.equals("") ? "?"+sortedQuery : "";
    }


}