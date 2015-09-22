begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.analysis.phonetic
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
operator|.
name|phonetic
package|;
end_package

begin_comment
comment|/**  * Ge&auml;nderter Algorithmus aus der Matching Toolbox von Rainer Schnell  * Java-Programmierung von J&ouml;rg Reiher  *  * Die KÃ¶lner Phonetik wurde fÃ¼r den Einsatz in Namensdatenbanken wie  * der Verwaltung eines Krankenhauses durch Martin Haase (Institut fÃ¼r  * Sprachwissenschaft, UniversitÃ¤t zu KÃ¶ln) und Kai Heitmann (Insitut fÃ¼r  * medizinische Statistik, Informatik und Epidemiologie, KÃ¶ln)  Ã¼berarbeitet.  * M. Haase und K. Heitmann. Die Erweiterte KÃ¶lner Phonetik. 526, 2000.  *  * nach: Martin Wilz, Aspekte der Kodierung phonetischer Ãhnlichkeiten  * in deutschen Eigennamen, Magisterarbeit.  * http://www.uni-koeln.de/phil-fak/phonetik/Lehre/MA-Arbeiten/magister_wilz.pdf  *   * @author<a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>  */
end_comment

begin_class
DECL|class|HaasePhonetik
specifier|public
class|class
name|HaasePhonetik
extends|extends
name|KoelnerPhonetik
block|{
DECL|field|HAASE_VARIATIONS_PATTERNS
specifier|private
specifier|final
specifier|static
name|String
index|[]
name|HAASE_VARIATIONS_PATTERNS
init|=
block|{
literal|"OWN"
block|,
literal|"RB"
block|,
literal|"WSK"
block|,
literal|"A$"
block|,
literal|"O$"
block|,
literal|"SCH"
block|,
literal|"GLI"
block|,
literal|"EAU$"
block|,
literal|"^CH"
block|,
literal|"AUX"
block|,
literal|"EUX"
block|,
literal|"ILLE"
block|}
decl_stmt|;
DECL|field|HAASE_VARIATIONS_REPLACEMENTS
specifier|private
specifier|final
specifier|static
name|String
index|[]
name|HAASE_VARIATIONS_REPLACEMENTS
init|=
block|{
literal|"AUN"
block|,
literal|"RW"
block|,
literal|"RSK"
block|,
literal|"AR"
block|,
literal|"OW"
block|,
literal|"CH"
block|,
literal|"LI"
block|,
literal|"O"
block|,
literal|"SCH"
block|,
literal|"O"
block|,
literal|"O"
block|,
literal|"I"
block|}
decl_stmt|;
annotation|@
name|Override
DECL|method|getPatterns
specifier|protected
name|String
index|[]
name|getPatterns
parameter_list|()
block|{
return|return
name|HAASE_VARIATIONS_PATTERNS
return|;
block|}
annotation|@
name|Override
DECL|method|getReplacements
specifier|protected
name|String
index|[]
name|getReplacements
parameter_list|()
block|{
return|return
name|HAASE_VARIATIONS_REPLACEMENTS
return|;
block|}
annotation|@
name|Override
DECL|method|getCode
specifier|protected
name|char
name|getCode
parameter_list|()
block|{
return|return
literal|'9'
return|;
block|}
block|}
end_class

end_unit

