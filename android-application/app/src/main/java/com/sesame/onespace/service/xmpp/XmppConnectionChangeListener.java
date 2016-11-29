package com.sesame.onespace.service.xmpp;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

/**
 * Created by chongos on 10/22/15 AD.
 */
public abstract class XmppConnectionChangeListener {
    
    public abstract void newConnection(AbstractXMPPConnection connection);

}
