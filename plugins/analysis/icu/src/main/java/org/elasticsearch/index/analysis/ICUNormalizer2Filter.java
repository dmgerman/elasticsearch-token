begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.analysis
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
package|;
end_package

begin_import
import|import
name|com
operator|.
name|ibm
operator|.
name|icu
operator|.
name|text
operator|.
name|Normalizer
import|;
end_import

begin_import
import|import
name|com
operator|.
name|ibm
operator|.
name|icu
operator|.
name|text
operator|.
name|Normalizer2
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|TokenFilter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|TokenStream
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|tokenattributes
operator|.
name|TermAttribute
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|CharSequenceTermAttribute
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Normalize token text with ICU's {@link com.ibm.icu.text.Normalizer2}  *<p>  * With this filter, you can normalize text in the following ways:  *<ul>  *<li> NFKC Normalization, Case Folding, and removing Ignorables (the default)  *<li> Using a standard Normalization mode (NFC, NFD, NFKC, NFKD)  *<li> Based on rules from a custom normalization mapping.  *</ul>  *<p>  * If you use the defaults, this filter is a simple way to standardize Unicode text  * in a language-independent way for search:  *<ul>  *<li> The case folding that it does can be seen as a replacement for  * LowerCaseFilter.  *<li> Ignorables such as Zero-Width Joiner and Variation Selectors are removed.  * These are typically modifier characters that affect display.  *</ul>  *  * @see com.ibm.icu.text.Normalizer2  * @see com.ibm.icu.text.FilteredNormalizer2  */
end_comment

begin_comment
comment|// TODO Lucene Monitor: Once 3.1 is released use it instead
end_comment

begin_class
DECL|class|ICUNormalizer2Filter
specifier|public
class|class
name|ICUNormalizer2Filter
extends|extends
name|TokenFilter
block|{
DECL|field|termAtt
specifier|private
specifier|final
name|TermAttribute
name|termAtt
init|=
name|addAttribute
argument_list|(
name|TermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|normalizer
specifier|private
specifier|final
name|Normalizer2
name|normalizer
decl_stmt|;
DECL|field|buffer
specifier|private
specifier|final
name|StringBuilder
name|buffer
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
DECL|field|charSequenceTermAtt
specifier|private
specifier|final
name|CharSequenceTermAttribute
name|charSequenceTermAtt
decl_stmt|;
comment|/**      * Create a new Normalizer2Filter that combines NFKC normalization, Case      * Folding, and removes Default Ignorables (NFKC_Casefold)      */
DECL|method|ICUNormalizer2Filter
specifier|public
name|ICUNormalizer2Filter
parameter_list|(
name|TokenStream
name|input
parameter_list|)
block|{
name|this
argument_list|(
name|input
argument_list|,
name|Normalizer2
operator|.
name|getInstance
argument_list|(
literal|null
argument_list|,
literal|"nfkc_cf"
argument_list|,
name|Normalizer2
operator|.
name|Mode
operator|.
name|COMPOSE
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Create a new Normalizer2Filter with the specified Normalizer2      *      * @param input      stream      * @param normalizer normalizer to use      */
DECL|method|ICUNormalizer2Filter
specifier|public
name|ICUNormalizer2Filter
parameter_list|(
name|TokenStream
name|input
parameter_list|,
name|Normalizer2
name|normalizer
parameter_list|)
block|{
name|super
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|this
operator|.
name|normalizer
operator|=
name|normalizer
expr_stmt|;
name|this
operator|.
name|charSequenceTermAtt
operator|=
operator|new
name|CharSequenceTermAttribute
argument_list|(
name|termAtt
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|incrementToken
specifier|public
specifier|final
name|boolean
name|incrementToken
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|input
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
if|if
condition|(
name|normalizer
operator|.
name|quickCheck
argument_list|(
name|charSequenceTermAtt
argument_list|)
operator|!=
name|Normalizer
operator|.
name|YES
condition|)
block|{
name|buffer
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|normalizer
operator|.
name|normalize
argument_list|(
name|charSequenceTermAtt
argument_list|,
name|buffer
argument_list|)
expr_stmt|;
name|termAtt
operator|.
name|setTermBuffer
argument_list|(
name|buffer
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

