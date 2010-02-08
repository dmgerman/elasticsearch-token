begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query.json
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|json
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|json
operator|.
name|JsonBuilder
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
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|FilteredQueryJsonQueryBuilder
specifier|public
class|class
name|FilteredQueryJsonQueryBuilder
extends|extends
name|BaseJsonQueryBuilder
block|{
DECL|field|queryBuilder
specifier|private
specifier|final
name|JsonQueryBuilder
name|queryBuilder
decl_stmt|;
DECL|field|filterBuilder
specifier|private
specifier|final
name|JsonFilterBuilder
name|filterBuilder
decl_stmt|;
DECL|field|boost
specifier|private
name|float
name|boost
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|FilteredQueryJsonQueryBuilder
specifier|public
name|FilteredQueryJsonQueryBuilder
parameter_list|(
name|JsonQueryBuilder
name|queryBuilder
parameter_list|,
name|JsonFilterBuilder
name|filterBuilder
parameter_list|)
block|{
name|this
operator|.
name|queryBuilder
operator|=
name|queryBuilder
expr_stmt|;
name|this
operator|.
name|filterBuilder
operator|=
name|filterBuilder
expr_stmt|;
block|}
DECL|method|boost
specifier|public
name|FilteredQueryJsonQueryBuilder
name|boost
parameter_list|(
name|float
name|boost
parameter_list|)
block|{
name|this
operator|.
name|boost
operator|=
name|boost
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|doJson
annotation|@
name|Override
specifier|protected
name|void
name|doJson
parameter_list|(
name|JsonBuilder
name|builder
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|FilteredQueryJsonQueryParser
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"query"
argument_list|)
expr_stmt|;
name|queryBuilder
operator|.
name|toJson
argument_list|(
name|builder
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"filter"
argument_list|)
expr_stmt|;
name|filterBuilder
operator|.
name|toJson
argument_list|(
name|builder
argument_list|)
expr_stmt|;
if|if
condition|(
name|boost
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"boost"
argument_list|,
name|boost
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

