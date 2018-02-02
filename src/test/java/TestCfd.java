
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mx.bigdata.sat.cfdiv.CFDv33;
import mx.bigdata.sat.security.factory.v.KeyLoaderFactory;
import mx.bigdata.sat.security.v.PrivateKeyLoader;

/*
 * Copyright 2017 user.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *
 * @author user
 */
public class TestCfd {
    public final static String CONTEXT_COMPLEMENTO_NOMINA = "mx.bigdata.sat.common.nomina12.schema";
    public final static String CONTEXT_COMPLEMENTO_IMPLOCAL = "mx.bigdata.sat.common.implocal.schema";
    public final static String CONTEXT_ADDENDA_SANOFI_V1 = "mx.bigdata.sat.addenda.sanofi.schema";
    public final static String CONTEXT_ADDENDA_VW_PMT_V1 = "mx.bigdata.sat.addenda.vwpmt.schema";
    public final static String CONTEXT_ADDENDA_CHRYSLER_PUA_V1 = "mx.bigdata.sat.addenda.chryslerpua.schema";
    public final static String CONTEXT_ADDENDA_GM_V13 = "mx.bigdata.sat.addenda.gm.v13.schema";
    public final static String CONTEXT_ADDENDA_FORD_FOM_V1 = "mx.bigdata.sat.addenda.fordfom.schema";
    public final static String CONTEXT_ADDENDA_CHRYSLER_PPY_V1 = "mx.bigdata.sat.addenda.chryslerppy.schema";
    public final static String[] CONTEXT_ARRAY_COMPLEMENTOS = {CONTEXT_COMPLEMENTO_NOMINA, 
                                                    CONTEXT_COMPLEMENTO_IMPLOCAL};
    public final static String[] CONTEXT_ARRAY_ALL = {CONTEXT_COMPLEMENTO_NOMINA, 
                                                    CONTEXT_COMPLEMENTO_IMPLOCAL,
                                                    CONTEXT_ADDENDA_SANOFI_V1,
                                                    CONTEXT_ADDENDA_VW_PMT_V1,
                                                    CONTEXT_ADDENDA_CHRYSLER_PUA_V1,
                                                    CONTEXT_ADDENDA_GM_V13,
                                                    CONTEXT_ADDENDA_FORD_FOM_V1,
                                                    CONTEXT_ADDENDA_CHRYSLER_PPY_V1};
    
    public final static String[] NAMESPACE_AND_PREFIX_COMPLEMENTO_NOMINA = {"http://www.sat.gob.mx/nomina12", "nomina12"};
    public final static String[] NAMESPACE_AND_PREFIX_COMPLEMENTO_IMPLOCAL = {"http://www.sat.gob.mx/implocal", "implocal"};
    public final static String[] NAMESPACE_AND_PREFIX_ADDENDA_SANOFI_V1 = {"https://mexico.sanofi.com/schemas", "sanofi"};
    public final static String[] NAMESPACE_AND_PREFIX_ADDENDA_VW_PMT_V1 = {"http://www.vwnovedades.com/volkswagen/kanseilab/shcp/2009/Addenda/PMT", "PMT"};
    public final static String[] NAMESPACE_AND_PREFIX_ADDENDA_FORD_FOM_V1 = {"http://www.ford.com.mx/cfdi/addenda", "fomadd"};
    public final static List<String[]> NAMESPACE_AND_PREFIXES_COMPLEMENTOS = new ArrayList<String[]>(Arrays.asList(
                                NAMESPACE_AND_PREFIX_COMPLEMENTO_NOMINA,
                                NAMESPACE_AND_PREFIX_COMPLEMENTO_IMPLOCAL));
    public final static List<String[]> NAMESPACE_AND_PREFIXES_ALL = new ArrayList<String[]>(Arrays.asList(
                                NAMESPACE_AND_PREFIX_COMPLEMENTO_NOMINA,
                                NAMESPACE_AND_PREFIX_COMPLEMENTO_IMPLOCAL,
                                NAMESPACE_AND_PREFIX_ADDENDA_SANOFI_V1,
                                NAMESPACE_AND_PREFIX_ADDENDA_VW_PMT_V1,
                                NAMESPACE_AND_PREFIX_ADDENDA_FORD_FOM_V1));
    public static void main(String args[]) throws Exception{
        
        CFDv33 cfd=new CFDv33(new FileInputStream("C:\\Users\\user\\Desktop\\Instalador V3 plus\\repositorio\\1494631442180.xml"),TestCfd.CONTEXT_ARRAY_COMPLEMENTOS);
        //Recuperamos objetos de Certificado y Llave privada del emisor
        java.security.cert.X509Certificate certX509 = null;
        java.security.PrivateKey pkey = null;
        try{
             certX509 = KeyLoaderFactory.loadX509Certificate(new FileInputStream("C:\\Users\\user\\Desktop\\Instalador V3 plus\\Monitor_Fens_Dist\\mli0809307s7.cer"));
             PrivateKeyLoader privateKey=new PrivateKeyLoader(new FileInputStream("C:\\Users\\user\\Desktop\\Instalador V3 plus\\Monitor_Fens_Dist\\CSD_UNICA_MLI0809307S7_20131121_091247.key"), "bolo0507");
             pkey=privateKey.getKey();
             //pkey = PrivateKeyLoader.loadPKCS8PrivateKey(new FileInputStream("C:\\Users\\user\\Desktop\\Instalador V3 plus\\Monitor_Fens_Dist\\CSD_UNICA_MLI0809307S7_20131121_091247.key"), "bolo0507");
        }catch(Exception ex){
            throw new Exception("El emisor de RFC '' configurado"
                    + " no tiene Certificado y Llave privada validos." + ex.toString());
        }
        cfd.sellar(pkey, certX509);
        for (String[] namespacePrefix : TestCfd.NAMESPACE_AND_PREFIXES_COMPLEMENTOS){
            cfd.addNamespace(namespacePrefix[0], namespacePrefix[1]);
        }
        cfd.validar();
        cfd.verificar();
    }
}
