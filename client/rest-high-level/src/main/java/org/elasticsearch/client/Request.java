begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpEntity
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|get
operator|.
name|GetRequest
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
name|lucene
operator|.
name|uid
operator|.
name|Versions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|VersionType
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
name|FetchSourceContext
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|Locale
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|StringJoiner
import|;
end_import

begin_class
DECL|class|Request
specifier|final
class|class
name|Request
block|{
DECL|field|method
specifier|final
name|String
name|method
decl_stmt|;
DECL|field|endpoint
specifier|final
name|String
name|endpoint
decl_stmt|;
DECL|field|params
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
decl_stmt|;
DECL|field|entity
specifier|final
name|HttpEntity
name|entity
decl_stmt|;
DECL|method|Request
name|Request
parameter_list|(
name|String
name|method
parameter_list|,
name|String
name|endpoint
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|,
name|HttpEntity
name|entity
parameter_list|)
block|{
name|this
operator|.
name|method
operator|=
name|method
expr_stmt|;
name|this
operator|.
name|endpoint
operator|=
name|endpoint
expr_stmt|;
name|this
operator|.
name|params
operator|=
name|params
expr_stmt|;
name|this
operator|.
name|entity
operator|=
name|entity
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Request{"
operator|+
literal|"method='"
operator|+
name|method
operator|+
literal|'\''
operator|+
literal|", endpoint='"
operator|+
name|endpoint
operator|+
literal|'\''
operator|+
literal|", params="
operator|+
name|params
operator|+
literal|'}'
return|;
block|}
DECL|method|ping
specifier|static
name|Request
name|ping
parameter_list|()
block|{
return|return
operator|new
name|Request
argument_list|(
literal|"HEAD"
argument_list|,
literal|"/"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
DECL|method|exists
specifier|static
name|Request
name|exists
parameter_list|(
name|GetRequest
name|getRequest
parameter_list|)
block|{
return|return
operator|new
name|Request
argument_list|(
literal|"HEAD"
argument_list|,
name|getEndpoint
argument_list|(
name|getRequest
argument_list|)
argument_list|,
name|getParams
argument_list|(
name|getRequest
argument_list|)
argument_list|,
literal|null
argument_list|)
return|;
block|}
DECL|method|get
specifier|static
name|Request
name|get
parameter_list|(
name|GetRequest
name|getRequest
parameter_list|)
block|{
return|return
operator|new
name|Request
argument_list|(
literal|"GET"
argument_list|,
name|getEndpoint
argument_list|(
name|getRequest
argument_list|)
argument_list|,
name|getParams
argument_list|(
name|getRequest
argument_list|)
argument_list|,
literal|null
argument_list|)
return|;
block|}
DECL|method|getParams
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getParams
parameter_list|(
name|GetRequest
name|getRequest
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|putParam
argument_list|(
literal|"preference"
argument_list|,
name|getRequest
operator|.
name|preference
argument_list|()
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|putParam
argument_list|(
literal|"routing"
argument_list|,
name|getRequest
operator|.
name|routing
argument_list|()
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|putParam
argument_list|(
literal|"parent"
argument_list|,
name|getRequest
operator|.
name|parent
argument_list|()
argument_list|,
name|params
argument_list|)
expr_stmt|;
if|if
condition|(
name|getRequest
operator|.
name|refresh
argument_list|()
condition|)
block|{
name|params
operator|.
name|put
argument_list|(
literal|"refresh"
argument_list|,
name|Boolean
operator|.
name|TRUE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getRequest
operator|.
name|realtime
argument_list|()
operator|==
literal|false
condition|)
block|{
name|params
operator|.
name|put
argument_list|(
literal|"realtime"
argument_list|,
name|Boolean
operator|.
name|FALSE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getRequest
operator|.
name|storedFields
argument_list|()
operator|!=
literal|null
operator|&&
name|getRequest
operator|.
name|storedFields
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|params
operator|.
name|put
argument_list|(
literal|"stored_fields"
argument_list|,
name|String
operator|.
name|join
argument_list|(
literal|","
argument_list|,
name|getRequest
operator|.
name|storedFields
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getRequest
operator|.
name|version
argument_list|()
operator|!=
name|Versions
operator|.
name|MATCH_ANY
condition|)
block|{
name|params
operator|.
name|put
argument_list|(
literal|"version"
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|getRequest
operator|.
name|version
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getRequest
operator|.
name|versionType
argument_list|()
operator|!=
name|VersionType
operator|.
name|INTERNAL
condition|)
block|{
name|params
operator|.
name|put
argument_list|(
literal|"version_type"
argument_list|,
name|getRequest
operator|.
name|versionType
argument_list|()
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getRequest
operator|.
name|fetchSourceContext
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|FetchSourceContext
name|fetchSourceContext
init|=
name|getRequest
operator|.
name|fetchSourceContext
argument_list|()
decl_stmt|;
if|if
condition|(
name|fetchSourceContext
operator|.
name|fetchSource
argument_list|()
operator|==
literal|false
condition|)
block|{
name|params
operator|.
name|put
argument_list|(
literal|"_source"
argument_list|,
name|Boolean
operator|.
name|FALSE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fetchSourceContext
operator|.
name|includes
argument_list|()
operator|!=
literal|null
operator|&&
name|fetchSourceContext
operator|.
name|includes
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|params
operator|.
name|put
argument_list|(
literal|"_source_include"
argument_list|,
name|String
operator|.
name|join
argument_list|(
literal|","
argument_list|,
name|fetchSourceContext
operator|.
name|includes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fetchSourceContext
operator|.
name|excludes
argument_list|()
operator|!=
literal|null
operator|&&
name|fetchSourceContext
operator|.
name|excludes
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|params
operator|.
name|put
argument_list|(
literal|"_source_exclude"
argument_list|,
name|String
operator|.
name|join
argument_list|(
literal|","
argument_list|,
name|fetchSourceContext
operator|.
name|excludes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|params
argument_list|)
return|;
block|}
DECL|method|getEndpoint
specifier|private
specifier|static
name|String
name|getEndpoint
parameter_list|(
name|GetRequest
name|getRequest
parameter_list|)
block|{
name|StringJoiner
name|pathJoiner
init|=
operator|new
name|StringJoiner
argument_list|(
literal|"/"
argument_list|,
literal|"/"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
return|return
name|pathJoiner
operator|.
name|add
argument_list|(
name|getRequest
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|getRequest
operator|.
name|type
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|getRequest
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|putParam
specifier|private
specifier|static
name|void
name|putParam
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|value
argument_list|)
condition|)
block|{
name|params
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
