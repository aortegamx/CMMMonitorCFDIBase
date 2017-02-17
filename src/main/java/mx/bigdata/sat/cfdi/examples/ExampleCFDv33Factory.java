/*
 *  Copyright 2010-2011 BigData.mx
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

package mx.bigdata.sat.cfdi.examples;

import mx.bigdata.sat.cfdi.v33.schema.*;
import mx.bigdata.sat.cfdi.v33.schema.Comprobante.*;
import mx.bigdata.sat.cfdi.v33.schema.Comprobante.Conceptos.Concepto;
import mx.bigdata.sat.cfdi.v33.schema.Comprobante.Impuestos.Traslados;
import mx.bigdata.sat.cfdi.v33.schema.Comprobante.Impuestos.Traslados.Traslado;

import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public final class ExampleCFDv33Factory {
    
  public static Comprobante createComprobante() throws Exception {
    ObjectFactory of = new ObjectFactory();
    Comprobante comp = of.createComprobante();
    comp.setVersion("3.3");
    comp.setMoneda(CMoneda.MXN);
    Date date = new GregorianCalendar(2017, 1, 6, 20, 38, 12).getTime();
    comp.setFecha(date);
    comp.setFormaPago("01");
    comp.setSubTotal(new BigDecimal("466.43"));
    comp.setTotal(new BigDecimal("488.50"));
    comp.setTipoDeComprobante(CTipoDeComprobante.I);
    comp.setMetodoPago(CMetodoPago.PUE);
    comp.setLugarExpedicion("45079");
    comp.setEmisor(createEmisor(of));
    comp.setReceptor(createReceptor(of));
    comp.setConceptos(createConceptos(of));
    comp.setImpuestos(createImpuestos(of));
    comp.setComplemento(createComplemento(of));
    return comp;
  }
  
  private static Complemento createComplemento(ObjectFactory of){
	  Complemento complemento = new Complemento();
	  
	  
	  return complemento;
  }
    
  private static Emisor createEmisor(ObjectFactory of) {
    Emisor emisor = of.createComprobanteEmisor();
    emisor.setNombre("PHARMA PLUS SA DE CV");
    emisor.setRfc("PPL961114GZ1");
    emisor.setRegimenFiscal("601");
    return emisor;
  }

  private static Receptor createReceptor(ObjectFactory of) {
    Receptor receptor = of.createComprobanteReceptor();
    receptor.setNombre("JUAN PEREZ PEREZ");
    receptor.setRfc("PEPJ8001019Q8");
    receptor.setResidenciaFiscal(CPais.MEX);
    receptor.setUsoCFDI(CUsoCFDI.D_01);
    return receptor;
  }

  private static Conceptos createConceptos(ObjectFactory of) {
    Conceptos cps = of.createComprobanteConceptos();
    List<Concepto> list = cps.getConcepto(); 
    Concepto c1 = of.createComprobanteConceptosConcepto();
    c1.setClaveProdServ("01010101");
    c1.setClaveUnidad("F52");
    c1.setUnidad("CAPSULAS");
    c1.setImporte(new BigDecimal("244.00"));
    c1.setCantidad(new BigDecimal("1.0"));
    c1.setDescripcion("VIBRAMICINA 100MG 10");
    c1.setValorUnitario(new BigDecimal("244.00"));
    list.add(c1);
    Concepto c2 = of.createComprobanteConceptosConcepto();
    c2.setClaveProdServ("01010101");
    c2.setClaveUnidad("F52");
    c2.setUnidad("BOTELLA");
    c2.setImporte(new BigDecimal("137.93"));
    c2.setCantidad(new BigDecimal("1.0"));
    c2.setDescripcion("CLORUTO 500M");
    c2.setValorUnitario(new BigDecimal("137.93"));
    list.add(c2);
    Concepto c3 = of.createComprobanteConceptosConcepto();
    c3.setClaveProdServ("01010101");
    c3.setUnidad("TABLETAS");
    c3.setClaveUnidad("F52");
    c3.setImporte(new BigDecimal("84.50"));
    c3.setCantidad(new BigDecimal("1.0"));
    c3.setDescripcion("SEDEPRON 250MG 10");
    c3.setValorUnitario(new BigDecimal("84.50"));
    list.add(c3);
    return cps;
  }

  private static Impuestos createImpuestos(ObjectFactory of) {
    Impuestos imps = of.createComprobanteImpuestos();

    Traslados trs = of.createComprobanteImpuestosTraslados();
    List<Traslado> list = trs.getTraslado();

    Traslado t1 = of.createComprobanteImpuestosTrasladosTraslado();
    t1.setImporte(new BigDecimal("36"));
    t1.setImpuesto("001");
    t1.setTasaOCuota("0.160000");
    t1.setTipoFactor(CTipoFactor.TASA);
    list.add(t1);

    Traslado t2 = of.createComprobanteImpuestosTrasladosTraslado();
    t2.setImporte(new BigDecimal("22.07"));
    t2.setImpuesto("002");
    t2.setTasaOCuota("0.160000");
    t2.setTipoFactor(CTipoFactor.TASA);
    list.add(t2);

    imps.setTraslados(trs);

    return imps;
  }
}