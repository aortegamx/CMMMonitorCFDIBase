/*
 *  Copyright 2011 BigData.mx
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package mx.bigdata.sat.cfdiv;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import mx.bigdata.sat.cfdi.v33.schema.Comprobante;
import mx.bigdata.sat.cfdi.v33.schema.ObjectFactory;
import mx.bigdata.sat.common.ComprobanteBase;
import mx.bigdata.sat.common.NamespacePrefixMapperImpl;
import mx.bigdata.sat.common.SatCharacterEscapeHandler;
import mx.bigdata.sat.common.URIResolverImpl;
import mx.bigdata.sat.security.v.KeyLoaderEnumeration;
import mx.bigdata.sat.security.factory.v.KeyLoaderFactory;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import mx.bigdata.sat.security.v.KeyLoader;

public final class CFDv33 implements CFDI {

  private static final String XSLT = "/xslt/cadenaoriginal_3_3.xslt";

  private static final String[] XSD = new String[] {
      "/xsd/v33/catCFDI.xsd",
      "/xsd/v33/tdCFDI.xsd",
      "/xsd/v33/cfdv33.xsd",
      "/xsd/v33/catPagos.xsd",
      "/xsd/v33/Pagos10.xsd",
      "/xsd/v33/TimbreFiscalDigitalv11.xsd",
      "/xsd/common/TuristaPasajeroExtranjero/TuristaPasajeroExtranjero.xsd",
      "/xsd/common/divisas/divisas.xsd",
      "/xsd/common/donat/donat11.xsd",
      "/xsd/common/ecb/ecb.xsd",
      "/xsd/common/ecc/ecc.xsd",
      "/xsd/common/iedu/iedu.xsd",
      "/xsd/common/implocal/implocal.xsd",
      "/xsd/common/leyendasFisc/leyendasFisc.xsd",
      "/xsd/common/pfic/pfic.xsd",
      "/xsd/common/psgcfdsp/psgcfdsp.xsd",
      "/xsd/common/psgecfd/psgecfd.xsd",
      "/xsd/common/terceros/terceros11.xsd",
      "/xsd/common/ventavehiculos/ventavehiculos.xsd",
      "/xsd/common/nomina12/nomina12.xsd",
      "/xsd/common/nomina12/catNomina.xsd"
      
  };

  private static final String XML_HEADER =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

  private static final String BASE_CONTEXT = "mx.bigdata.sat.cfdi.v33.schema";

  private final static Joiner JOINER = Joiner.on(':');

  private final JAXBContext context;

  public static final ImmutableMap<String, String> PREFIXES =
    ImmutableMap.of("http://www.w3.org/2001/XMLSchema-instance","xsi",
                    "http://www.sat.gob.mx/cfd/3", "cfdi",
                    "http://www.sat.gob.mx/sitio_internet/cfd/catalogos", "catCFDI",
                    "http://www.sat.gob.mx/sitio_internet/cfd/tipoDatos/tdCFDI", "tdCFDI",
                    "http://www.sat.gob.mx/TimbreFiscalDigital", "tfd");

  private final Map<String, String> localPrefixes = Maps.newHashMap(PREFIXES);

  private TransformerFactory tf;

  final Comprobante document;

  public CFDv33(InputStream in, String... contexts) throws Exception {
    this.context = getContext(contexts);
    this.document = load(in);
  }

  public CFDv33(Comprobante comprobante, String... contexts) throws Exception {
    this.context = getContext(contexts);
    this.document = copy(comprobante);
  }

  public void addNamespace(String uri, String prefix) {
    localPrefixes.put(uri, prefix);
  }

  public void setTransformerFactory(TransformerFactory tf) {
    this.tf = tf;   
    tf.setURIResolver(new URIResolverImpl()); 
  }

  public void sellar(PrivateKey key, X509Certificate cert) throws Exception {
    cert.checkValidity();

    BigInteger bi = cert.getSerialNumber();
    String noCertificado = new String(bi.toByteArray());
    document.setNoCertificado(noCertificado);

    String signature = getSignature(key);
    //System.out.println("Signature: " + signature);

    document.setSello(signature);
    byte[] bytes = cert.getEncoded();
    Base64 b64 = new Base64(-1);
    String certStr = b64.encodeToString(bytes);

    //System.out.println("Certificado: " + certStr);
    document.setCertificado(certStr);
  }
  
  public Comprobante sellarComprobante(PrivateKey key, X509Certificate cert) 
    throws Exception {
    sellar(key, cert);
    return doGetComprobante();
  }

  public void validar() throws Exception {
    validar(null);
  }

  public void validar(ErrorHandler handler) throws Exception {
    SchemaFactory sf =
      SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Source[] schemas = new Source[XSD.length];
    for (int i = 0; i < XSD.length; i++) {
      schemas[i] = new StreamSource(getClass().getResourceAsStream(XSD[i]));
    }
    Schema schema = sf.newSchema(schemas);
    Validator validator = schema.newValidator();
    if (handler != null) {
      validator.setErrorHandler(handler);
    }
    validator.validate(new JAXBSource(context, document));
  }

  public void verificar() throws Exception {
    String certStr = document.getCertificado();
    Base64 b64 = new Base64(-1);
    byte[] cbs = b64.decode(certStr);

    /*X509Certificate cert = KeyLoaderFactory.createInstance(
            KeyLoaderEnumeration.PUBLIC_KEY_LOADER,
            new ByteArrayInputStream(cbs)
    ).getKey();*/
    X509Certificate cert = KeyLoaderFactory
      .loadX509Certificate(new ByteArrayInputStream(cbs)); 

    String sigStr = document.getSello();
    byte[] signature = b64.decode(sigStr); 
    byte[] bytes = getOriginalBytes();

    //String cadenaOriginal = new String(bytes, "UTF8");
    //System.out.println("Cadena Original: " + cadenaOriginal);

    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initVerify(cert);
    sig.update(bytes);
    boolean bool = sig.verify(signature);
    if (!bool) {
      throw new Exception("Invalid signature");
    }
  }

  public void guardar(OutputStream out) throws Exception {
    Marshaller m = context.createMarshaller();
    /*m.setProperty(CharacterEscapeHandler.class.getName(),  new SatCharacterEscapeHandler());
    m.setProperty(Marshaller.JAXB_ENCODING, "ASCII");*/
    m.setProperty("com.sun.xml.bind.namespacePrefixMapper",
                  new NamespacePrefixMapperImpl(localPrefixes));
    m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, 
                  "http://www.sat.gob.mx/cfd/3  " +
                  "http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv33.xsd " +
                  "http://www.sat.gob.mx/sitio_internet/cfd/catalogos " +
                  "http://www.sat.gob.mx/sitio_internet/cfd/tipoDatos/tdCFDI "+
                  "http://www.sat.gob.mx/Pagos "+
                  "http://www.sat.gob.mx/sitio_internet/cfd/Pagos/Pagos10.xsd");
    byte[] xmlHeaderBytes = XML_HEADER.getBytes("UTF8");
    out.write(xmlHeaderBytes); 
    m.marshal(document, out);
  }

  public String getCadenaOriginal(X509Certificate cert) throws Exception {
    BigInteger bi = cert.getSerialNumber();
    String noCertificado = new String(bi.toByteArray());
    document.setNoCertificado(noCertificado);

    byte[] bytes = getOriginalBytes();
    return new String(bytes, "UTF8");
  }

  public String getCadenaOriginal() throws Exception {
    byte[] bytes = getOriginalBytes();
    return new String(bytes, "UTF8");
  }

  public static Comprobante newComprobante(InputStream in) throws Exception {
    return load(in);
  }

  byte[] getOriginalBytes() throws Exception {
    JAXBSource in = new JAXBSource(context, document);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Result out = new StreamResult(baos);
    TransformerFactory factory = tf;
    if (factory == null) {
      factory = TransformerFactory.newInstance();
      factory.setURIResolver(new URIResolverImpl());
    }
    Transformer transformer = factory
      .newTransformer(new StreamSource(getClass().getResourceAsStream(XSLT)));
    transformer.transform(in, out);
    return baos.toByteArray();
  }
    
  String getSignature(PrivateKey key) throws Exception {
    byte[] bytes = getOriginalBytes();
    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initSign(key);
    sig.update(bytes);
    byte[] signed = sig.sign();
    Base64 b64 = new Base64(-1);
    return b64.encodeToString(signed);
  }

  public ComprobanteBase getComprobante() throws Exception {
    return new CFDv33ComprobanteBase(doGetComprobante());
  }
  
  public Comprobante doGetComprobante() throws Exception {
    return copy(document);
  }

  // Defensive deep-copy
  private Comprobante copy(Comprobante comprobante) throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder db = dbf.newDocumentBuilder(); 
    Document doc = db.newDocument();
    Marshaller m = context.createMarshaller();
    m.marshal(comprobante, doc);
    Unmarshaller u = context.createUnmarshaller();
    return (Comprobante) u.unmarshal(doc);
  }
  
  public static final class CFDv33ComprobanteBase implements ComprobanteBase {

    private final Comprobante document;
    
    public CFDv33ComprobanteBase(Comprobante document) {
      this.document = document;
    }
    
    public boolean hasComplemento() {
      return document.getComplemento() != null;
    } 
    
    /*public List<Object> getComplementoGetAny() {
      return document.getComplemento().getAny();
    } */
    //colocado por Leonardo Montes de Oca, 31/08/2017, ya que es una lista de complementos sobre otra lista:
    public List<Object> getComplementoGetAny(int lugarListaComplemento) {
      return document.getComplemento().get(lugarListaComplemento).getAny();
    } 
    
    public List<Object> getComplementoGetAny() {
      return document.getComplemento().get(0).getAny();
    } 
    
    public String getSello() {
      return document.getSello();
    }
    
    public void setComplemento(Element element) {
      ObjectFactory of = new ObjectFactory();
      Comprobante.Complemento comp = of.createComprobanteComplemento();
      List<Object> list = comp.getAny(); 
      list.add(element);
      //document.setComplemento(comp)
      document.getComplemento().add(comp);
    }
    
    public Object getComprobante() {
      return document;
    }
  }
  
  private static JAXBContext getContext(String[] contexts) throws Exception {
    List<String> ctx = Lists.asList(BASE_CONTEXT, contexts);
    return JAXBContext.newInstance(JOINER.join(ctx));
  }

  private static Comprobante load(InputStream source, String... contexts) 
    throws Exception {
    JAXBContext context = getContext(contexts);
    try {
      Unmarshaller u = context.createUnmarshaller();
      return (Comprobante) u.unmarshal(source);
    } finally {
      source.close();
    }
  }

  static void dump(String title, byte[] bytes, PrintStream out) {
    out.printf("%s: ", title);
    for (byte b : bytes) {
      out.printf("%02x ", b & 0xff);
    }
    out.println();
  }
}