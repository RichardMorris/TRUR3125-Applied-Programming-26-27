package org.singsurf.jns3.data_link_layer;
import java.util.List;
public class FrameDecoder {

    public String decode(EthernetFrame frame) {
        return new String(frame.getPayload());
    }

    public String decode(List<EthernetFrame> frames) {
        StringBuilder sb = new StringBuilder();
        for (EthernetFrame frame : frames) {
            sb.append(new String(frame.getPayload()));
        }
        return sb.toString();
    }
}
