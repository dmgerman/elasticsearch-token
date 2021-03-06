begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|OffsetAttribute
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|subphase
operator|.
name|highlight
operator|.
name|FastVectorHighlighter
import|;
end_import

begin_interface
DECL|interface|TokenFilterFactory
specifier|public
interface|interface
name|TokenFilterFactory
block|{
DECL|method|name
name|String
name|name
parameter_list|()
function_decl|;
DECL|method|create
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|)
function_decl|;
comment|/**      * Does this analyzer mess up the {@link OffsetAttribute}s in such as way as to break the      * {@link FastVectorHighlighter}? If this is {@code true} then the      * {@linkplain FastVectorHighlighter} will attempt to work around the broken offsets.      */
DECL|method|breaksFastVectorHighlighter
specifier|default
name|boolean
name|breaksFastVectorHighlighter
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_interface

end_unit

