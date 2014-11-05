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
name|Analyzer
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
comment|/**  *  */
end_comment

begin_class
DECL|class|NumericAnalyzer
specifier|public
specifier|abstract
class|class
name|NumericAnalyzer
parameter_list|<
name|T
extends|extends
name|NumericTokenizer
parameter_list|>
extends|extends
name|Analyzer
block|{
annotation|@
name|Override
DECL|method|createComponents
specifier|protected
name|TokenStreamComponents
name|createComponents
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
try|try
block|{
comment|// LUCENE 4 UPGRADE: in reusableTokenStream the buffer size was char[120]
comment|// Not sure if this is intentional or not
return|return
operator|new
name|TokenStreamComponents
argument_list|(
name|createNumericTokenizer
argument_list|(
operator|new
name|char
index|[
literal|32
index|]
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to create numeric tokenizer"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|createNumericTokenizer
specifier|protected
specifier|abstract
name|T
name|createNumericTokenizer
parameter_list|(
name|char
index|[]
name|buffer
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

