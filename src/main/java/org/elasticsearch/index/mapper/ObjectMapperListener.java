begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|object
operator|.
name|ObjectMapper
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_interface
DECL|interface|ObjectMapperListener
specifier|public
interface|interface
name|ObjectMapperListener
block|{
DECL|class|Aggregator
specifier|public
specifier|static
class|class
name|Aggregator
implements|implements
name|ObjectMapperListener
block|{
DECL|field|objectMappers
specifier|public
specifier|final
name|List
argument_list|<
name|ObjectMapper
argument_list|>
name|objectMappers
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectMapper
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|objectMapper
specifier|public
name|void
name|objectMapper
parameter_list|(
name|ObjectMapper
name|objectMapper
parameter_list|)
block|{
name|objectMappers
operator|.
name|add
argument_list|(
name|objectMapper
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|objectMapper
name|void
name|objectMapper
parameter_list|(
name|ObjectMapper
name|objectMapper
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

