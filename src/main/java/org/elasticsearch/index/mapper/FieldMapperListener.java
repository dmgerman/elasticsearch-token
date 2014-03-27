begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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

begin_class
DECL|class|FieldMapperListener
specifier|public
specifier|abstract
class|class
name|FieldMapperListener
block|{
DECL|class|Aggregator
specifier|public
specifier|static
class|class
name|Aggregator
extends|extends
name|FieldMapperListener
block|{
DECL|field|mappers
specifier|public
specifier|final
name|List
argument_list|<
name|FieldMapper
argument_list|>
name|mappers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|fieldMapper
specifier|public
name|void
name|fieldMapper
parameter_list|(
name|FieldMapper
name|fieldMapper
parameter_list|)
block|{
name|mappers
operator|.
name|add
argument_list|(
name|fieldMapper
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|fieldMapper
specifier|public
specifier|abstract
name|void
name|fieldMapper
parameter_list|(
name|FieldMapper
name|fieldMapper
parameter_list|)
function_decl|;
DECL|method|fieldMappers
specifier|public
name|void
name|fieldMappers
parameter_list|(
name|Iterable
argument_list|<
name|FieldMapper
argument_list|>
name|fieldMappers
parameter_list|)
block|{
for|for
control|(
name|FieldMapper
name|mapper
range|:
name|fieldMappers
control|)
block|{
name|fieldMapper
argument_list|(
name|mapper
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

