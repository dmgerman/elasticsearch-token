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

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|JsonFilterBuilders
specifier|public
specifier|abstract
class|class
name|JsonFilterBuilders
block|{
DECL|method|termFilter
specifier|public
specifier|static
name|TermJsonFilterBuilder
name|termFilter
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
return|return
operator|new
name|TermJsonFilterBuilder
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
return|;
block|}
DECL|method|termFilter
specifier|public
specifier|static
name|TermJsonFilterBuilder
name|termFilter
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|value
parameter_list|)
block|{
return|return
operator|new
name|TermJsonFilterBuilder
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
return|;
block|}
DECL|method|termFilter
specifier|public
specifier|static
name|TermJsonFilterBuilder
name|termFilter
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|value
parameter_list|)
block|{
return|return
operator|new
name|TermJsonFilterBuilder
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
return|;
block|}
DECL|method|termFilter
specifier|public
specifier|static
name|TermJsonFilterBuilder
name|termFilter
parameter_list|(
name|String
name|name
parameter_list|,
name|float
name|value
parameter_list|)
block|{
return|return
operator|new
name|TermJsonFilterBuilder
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
return|;
block|}
DECL|method|termFilter
specifier|public
specifier|static
name|TermJsonFilterBuilder
name|termFilter
parameter_list|(
name|String
name|name
parameter_list|,
name|double
name|value
parameter_list|)
block|{
return|return
operator|new
name|TermJsonFilterBuilder
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
return|;
block|}
DECL|method|prefixFilter
specifier|public
specifier|static
name|PrefixJsonFilterBuilder
name|prefixFilter
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
return|return
operator|new
name|PrefixJsonFilterBuilder
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
return|;
block|}
DECL|method|rangeFilter
specifier|public
specifier|static
name|RangeJsonFilterBuilder
name|rangeFilter
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|RangeJsonFilterBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|queryFilter
specifier|public
specifier|static
name|QueryJsonFilterBuilder
name|queryFilter
parameter_list|(
name|JsonQueryBuilder
name|queryBuilder
parameter_list|)
block|{
return|return
operator|new
name|QueryJsonFilterBuilder
argument_list|(
name|queryBuilder
argument_list|)
return|;
block|}
DECL|method|JsonFilterBuilders
specifier|private
name|JsonFilterBuilders
parameter_list|()
block|{      }
block|}
end_class

end_unit

