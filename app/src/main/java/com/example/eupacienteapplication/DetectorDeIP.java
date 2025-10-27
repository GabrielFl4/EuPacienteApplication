package com.example.eupacienteapplication;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;


import android.content.Context;
import android.net.DhcpInfo;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.RouteInfo;
import android.net.wifi.WifiManager;


import java.util.List;

public class DetectorDeIP {
    public static String detectarIpServidor(Context context) {
        String ip = detectarViaWifiGateway(context);
        if (ip != null && !ip.isEmpty()) return ip;

        ip = detectarViaConnectivityRoutes(context);
        return ip;
    }

    // Método 1: DHCP (gateway da rede Wi-Fi)
    private static String detectarViaWifiGateway(Context context) {
        try {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wm == null) return null;
            DhcpInfo dhcp = wm.getDhcpInfo();
            if (dhcp == null) return null;

            int gw = dhcp.gateway;
            if (gw == 0) return null;

            return intToIp(gw);
        } catch (Exception e) {
            return null;
        }
    }

    // Método 2: pega a rota padrão da rede ativa e extrai o gateway
    private static String detectarViaConnectivityRoutes(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
            if (cm == null) return null;
            Network active = cm.getActiveNetwork();
            if (active == null) return null;

            LinkProperties lp = cm.getLinkProperties(active);
            if (lp == null) return null;

            List<RouteInfo> rotas = lp.getRoutes();
            if (rotas == null) return null;

            for (RouteInfo r : rotas) {
                if (r.isDefaultRoute() && r.getGateway() != null) {
                    return r.getGateway().getHostAddress();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // Converte int (endian do Android) para "a.b.c.d"
    private static String intToIp(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                ((ip >> 24) & 0xFF);
    }
}
