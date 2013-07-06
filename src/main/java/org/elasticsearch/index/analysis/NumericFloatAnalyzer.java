begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|gnu
operator|.
name|trove
operator|.
name|map
operator|.
name|hash
operator|.
name|TIntObjectHashMap
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
name|util
operator|.
name|NumericUtils
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Reader
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|NumericFloatAnalyzer
specifier|public
class|class
name|NumericFloatAnalyzer
extends|extends
name|NumericAnalyzer
argument_list|<
name|NumericFloatTokenizer
argument_list|>
block|{
DECL|field|builtIn
specifier|private
specifier|final
specifier|static
name|TIntObjectHashMap
argument_list|<
name|NamedAnalyzer
argument_list|>
name|builtIn
decl_stmt|;
static|static
block|{
name|builtIn
operator|=
operator|new
name|TIntObjectHashMap
argument_list|<
name|NamedAnalyzer
argument_list|>
argument_list|()
expr_stmt|;
name|builtIn
operator|.
name|put
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
operator|new
name|NamedAnalyzer
argument_list|(
literal|"_float/max"
argument_list|,
name|AnalyzerScope
operator|.
name|GLOBAL
argument_list|,
operator|new
name|NumericFloatAnalyzer
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
literal|64
condition|;
name|i
operator|+=
literal|4
control|)
block|{
name|builtIn
operator|.
name|put
argument_list|(
name|i
argument_list|,
operator|new
name|NamedAnalyzer
argument_list|(
literal|"_float/"
operator|+
name|i
argument_list|,
name|AnalyzerScope
operator|.
name|GLOBAL
argument_list|,
operator|new
name|NumericFloatAnalyzer
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|buildNamedAnalyzer
specifier|public
specifier|static
name|NamedAnalyzer
name|buildNamedAnalyzer
parameter_list|(
name|int
name|precisionStep
parameter_list|)
block|{
name|NamedAnalyzer
name|namedAnalyzer
init|=
name|builtIn
operator|.
name|get
argument_list|(
name|precisionStep
argument_list|)
decl_stmt|;
if|if
condition|(
name|namedAnalyzer
operator|==
literal|null
condition|)
block|{
name|namedAnalyzer
operator|=
operator|new
name|NamedAnalyzer
argument_list|(
literal|"_float/"
operator|+
name|precisionStep
argument_list|,
name|AnalyzerScope
operator|.
name|INDEX
argument_list|,
operator|new
name|NumericFloatAnalyzer
argument_list|(
name|precisionStep
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|namedAnalyzer
return|;
block|}
DECL|field|precisionStep
specifier|private
specifier|final
name|int
name|precisionStep
decl_stmt|;
DECL|method|NumericFloatAnalyzer
specifier|public
name|NumericFloatAnalyzer
parameter_list|()
block|{
name|this
argument_list|(
name|NumericUtils
operator|.
name|PRECISION_STEP_DEFAULT
argument_list|)
expr_stmt|;
block|}
DECL|method|NumericFloatAnalyzer
specifier|public
name|NumericFloatAnalyzer
parameter_list|(
name|int
name|precisionStep
parameter_list|)
block|{
name|this
operator|.
name|precisionStep
operator|=
name|precisionStep
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createNumericTokenizer
specifier|protected
name|NumericFloatTokenizer
name|createNumericTokenizer
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|char
index|[]
name|buffer
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|NumericFloatTokenizer
argument_list|(
name|reader
argument_list|,
name|precisionStep
argument_list|,
name|buffer
argument_list|)
return|;
block|}
block|}
end_class

end_unit

