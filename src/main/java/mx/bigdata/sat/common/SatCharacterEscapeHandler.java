package mx.bigdata.sat.common;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.io.Writer;

public class SatCharacterEscapeHandler implements CharacterEscapeHandler {

    @Override
    public void escape(char[] ac, int start, int len, boolean flag, Writer writer) throws IOException {
        if(ac!=null){
            String xml = StringEscapeUtils.escapeXml(new String(new String(ac).getBytes("UTF-8"), "UTF-8"));
            writer.write(xml);
        }
    }

}