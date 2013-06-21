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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|NumericTokenStream
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
DECL|class|NumericLongTokenizer
specifier|public
class|class
name|NumericLongTokenizer
extends|extends
name|NumericTokenizer
block|{
DECL|method|NumericLongTokenizer
specifier|public
name|NumericLongTokenizer
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|int
name|precisionStep
parameter_list|,
name|char
index|[]
name|buffer
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|reader
argument_list|,
operator|new
name|NumericTokenStream
argument_list|(
name|precisionStep
argument_list|)
argument_list|,
name|buffer
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setValue
specifier|protected
name|void
name|setValue
parameter_list|(
name|NumericTokenStream
name|tokenStream
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|tokenStream
operator|.
name|setLongValue
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

