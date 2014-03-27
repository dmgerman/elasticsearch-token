begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.node
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|node
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|Strings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|regex
operator|.
name|Regex
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|DiscoveryNodeFilters
specifier|public
class|class
name|DiscoveryNodeFilters
block|{
DECL|enum|OpType
specifier|public
specifier|static
enum|enum
name|OpType
block|{
DECL|enum constant|AND
name|AND
block|,
DECL|enum constant|OR
name|OR
block|}
empty_stmt|;
DECL|method|buildFromSettings
specifier|public
specifier|static
name|DiscoveryNodeFilters
name|buildFromSettings
parameter_list|(
name|OpType
name|opType
parameter_list|,
name|String
name|prefix
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
return|return
name|buildFromKeyValue
argument_list|(
name|opType
argument_list|,
name|settings
operator|.
name|getByPrefix
argument_list|(
name|prefix
argument_list|)
operator|.
name|getAsMap
argument_list|()
argument_list|)
return|;
block|}
DECL|method|buildFromKeyValue
specifier|public
specifier|static
name|DiscoveryNodeFilters
name|buildFromKeyValue
parameter_list|(
name|OpType
name|opType
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|filters
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|bFilters
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|filters
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
index|[]
name|values
init|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|values
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|bFilters
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|bFilters
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|DiscoveryNodeFilters
argument_list|(
name|opType
argument_list|,
name|bFilters
argument_list|)
return|;
block|}
DECL|field|filters
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|filters
decl_stmt|;
DECL|field|opType
specifier|private
specifier|final
name|OpType
name|opType
decl_stmt|;
DECL|method|DiscoveryNodeFilters
name|DiscoveryNodeFilters
parameter_list|(
name|OpType
name|opType
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|filters
parameter_list|)
block|{
name|this
operator|.
name|opType
operator|=
name|opType
expr_stmt|;
name|this
operator|.
name|filters
operator|=
name|filters
expr_stmt|;
block|}
DECL|method|match
specifier|public
name|boolean
name|match
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|entry
range|:
name|filters
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|attr
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
index|[]
name|values
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"_ip"
operator|.
name|equals
argument_list|(
name|attr
argument_list|)
condition|)
block|{
for|for
control|(
name|String
name|value
range|:
name|values
control|)
block|{
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|value
argument_list|,
name|node
operator|.
name|getHostAddress
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|opType
operator|==
name|OpType
operator|.
name|OR
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|opType
operator|==
name|OpType
operator|.
name|AND
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
block|}
elseif|else
if|if
condition|(
literal|"_host"
operator|.
name|equals
argument_list|(
name|attr
argument_list|)
condition|)
block|{
for|for
control|(
name|String
name|value
range|:
name|values
control|)
block|{
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|value
argument_list|,
name|node
operator|.
name|getHostName
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|opType
operator|==
name|OpType
operator|.
name|OR
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|opType
operator|==
name|OpType
operator|.
name|AND
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|value
argument_list|,
name|node
operator|.
name|getHostAddress
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|opType
operator|==
name|OpType
operator|.
name|OR
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|opType
operator|==
name|OpType
operator|.
name|AND
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
block|}
elseif|else
if|if
condition|(
literal|"_id"
operator|.
name|equals
argument_list|(
name|attr
argument_list|)
condition|)
block|{
for|for
control|(
name|String
name|value
range|:
name|values
control|)
block|{
if|if
condition|(
name|node
operator|.
name|id
argument_list|()
operator|.
name|equals
argument_list|(
name|value
argument_list|)
condition|)
block|{
if|if
condition|(
name|opType
operator|==
name|OpType
operator|.
name|OR
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|opType
operator|==
name|OpType
operator|.
name|AND
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
block|}
elseif|else
if|if
condition|(
literal|"_name"
operator|.
name|equals
argument_list|(
name|attr
argument_list|)
operator|||
literal|"name"
operator|.
name|equals
argument_list|(
name|attr
argument_list|)
condition|)
block|{
for|for
control|(
name|String
name|value
range|:
name|values
control|)
block|{
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|value
argument_list|,
name|node
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|opType
operator|==
name|OpType
operator|.
name|OR
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|opType
operator|==
name|OpType
operator|.
name|AND
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
block|}
else|else
block|{
name|String
name|nodeAttributeValue
init|=
name|node
operator|.
name|attributes
argument_list|()
operator|.
name|get
argument_list|(
name|attr
argument_list|)
decl_stmt|;
if|if
condition|(
name|nodeAttributeValue
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|opType
operator|==
name|OpType
operator|.
name|AND
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
block|{
continue|continue;
block|}
block|}
for|for
control|(
name|String
name|value
range|:
name|values
control|)
block|{
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|value
argument_list|,
name|nodeAttributeValue
argument_list|)
condition|)
block|{
if|if
condition|(
name|opType
operator|==
name|OpType
operator|.
name|OR
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|opType
operator|==
name|OpType
operator|.
name|AND
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
block|}
block|}
if|if
condition|(
name|opType
operator|==
name|OpType
operator|.
name|OR
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
block|{
return|return
literal|true
return|;
block|}
block|}
comment|/**      * Generates a human-readable string for the DiscoverNodeFilters.      * Example: {@code _id:"id1 OR blah",name:"blah OR name2"}      */
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|entryCount
init|=
name|filters
operator|.
name|size
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|entry
range|:
name|filters
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|attr
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
index|[]
name|values
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|attr
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|":\""
argument_list|)
expr_stmt|;
name|int
name|valueCount
init|=
name|values
operator|.
name|length
decl_stmt|;
for|for
control|(
name|String
name|value
range|:
name|values
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|valueCount
operator|>
literal|1
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" "
operator|+
name|opType
operator|.
name|toString
argument_list|()
operator|+
literal|" "
argument_list|)
expr_stmt|;
block|}
name|valueCount
operator|--
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"\""
argument_list|)
expr_stmt|;
if|if
condition|(
name|entryCount
operator|>
literal|1
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|entryCount
operator|--
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

