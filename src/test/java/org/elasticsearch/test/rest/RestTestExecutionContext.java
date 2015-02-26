begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
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
name|logging
operator|.
name|ESLogger
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
name|logging
operator|.
name|Loggers
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|client
operator|.
name|RestClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|client
operator|.
name|RestException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|client
operator|.
name|RestResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|spec
operator|.
name|RestSpec
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|net
operator|.
name|InetSocketAddress
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
name|List
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
name|Set
import|;
end_import

begin_comment
comment|/**  * Execution context passed across the REST tests.  * Holds the REST client used to communicate with elasticsearch.  * Caches the last obtained test response and allows to stash part of it within variables  * that can be used as input values in following requests.  */
end_comment

begin_class
DECL|class|RestTestExecutionContext
specifier|public
class|class
name|RestTestExecutionContext
implements|implements
name|Closeable
block|{
DECL|field|logger
specifier|private
specifier|static
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|RestTestExecutionContext
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|stash
specifier|private
specifier|final
name|Stash
name|stash
init|=
operator|new
name|Stash
argument_list|()
decl_stmt|;
DECL|field|restSpec
specifier|private
specifier|final
name|RestSpec
name|restSpec
decl_stmt|;
DECL|field|restClient
specifier|private
name|RestClient
name|restClient
decl_stmt|;
DECL|field|response
specifier|private
name|RestResponse
name|response
decl_stmt|;
DECL|method|RestTestExecutionContext
specifier|public
name|RestTestExecutionContext
parameter_list|(
name|RestSpec
name|restSpec
parameter_list|)
block|{
name|this
operator|.
name|restSpec
operator|=
name|restSpec
expr_stmt|;
block|}
comment|/**      * Calls an elasticsearch api with the parameters and request body provided as arguments.      * Saves the obtained response in the execution context.      * @throws RestException if the returned status code is non ok      */
DECL|method|callApi
specifier|public
name|RestResponse
name|callApi
parameter_list|(
name|String
name|apiName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|,
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|bodies
parameter_list|)
throws|throws
name|IOException
throws|,
name|RestException
block|{
comment|//makes a copy of the parameters before modifying them for this specific request
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|requestParams
init|=
name|Maps
operator|.
name|newHashMap
argument_list|(
name|params
argument_list|)
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
name|requestParams
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|stash
operator|.
name|isStashedValue
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
name|entry
operator|.
name|setValue
argument_list|(
name|stash
operator|.
name|unstashValue
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|String
name|body
init|=
name|actualBody
argument_list|(
name|bodies
argument_list|)
decl_stmt|;
try|try
block|{
name|response
operator|=
name|callApiInternal
argument_list|(
name|apiName
argument_list|,
name|requestParams
argument_list|,
name|body
argument_list|)
expr_stmt|;
comment|//we always stash the last response body
name|stash
operator|.
name|stashValue
argument_list|(
literal|"body"
argument_list|,
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|response
return|;
block|}
catch|catch
parameter_list|(
name|RestException
name|e
parameter_list|)
block|{
name|response
operator|=
name|e
operator|.
name|restResponse
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
DECL|method|actualBody
specifier|private
name|String
name|actualBody
parameter_list|(
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|bodies
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|bodies
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|""
return|;
block|}
if|if
condition|(
name|bodies
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
name|bodyAsString
argument_list|(
name|stash
operator|.
name|unstashMap
argument_list|(
name|bodies
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
name|StringBuilder
name|bodyBuilder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|body
range|:
name|bodies
control|)
block|{
name|bodyBuilder
operator|.
name|append
argument_list|(
name|bodyAsString
argument_list|(
name|stash
operator|.
name|unstashMap
argument_list|(
name|body
argument_list|)
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
return|return
name|bodyBuilder
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|bodyAsString
specifier|private
name|String
name|bodyAsString
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|body
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|map
argument_list|(
name|body
argument_list|)
operator|.
name|string
argument_list|()
return|;
block|}
DECL|method|callApiInternal
specifier|private
name|RestResponse
name|callApiInternal
parameter_list|(
name|String
name|apiName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|,
name|String
name|body
parameter_list|)
throws|throws
name|IOException
throws|,
name|RestException
block|{
return|return
name|restClient
operator|.
name|callApi
argument_list|(
name|apiName
argument_list|,
name|params
argument_list|,
name|body
argument_list|)
return|;
block|}
comment|/**      * Extracts a specific value from the last saved response      */
DECL|method|response
specifier|public
name|Object
name|response
parameter_list|(
name|String
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|response
operator|.
name|evaluate
argument_list|(
name|path
argument_list|,
name|stash
argument_list|)
return|;
block|}
comment|/**      * Creates or updates the embedded REST client when needed. Needs to be called before each test.      */
DECL|method|resetClient
specifier|public
name|void
name|resetClient
parameter_list|(
name|InetSocketAddress
index|[]
name|addresses
parameter_list|,
name|Settings
name|settings
parameter_list|)
throws|throws
name|IOException
throws|,
name|RestException
block|{
if|if
condition|(
name|restClient
operator|==
literal|null
condition|)
block|{
name|restClient
operator|=
operator|new
name|RestClient
argument_list|(
name|restSpec
argument_list|,
name|settings
argument_list|,
name|addresses
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|//re-initialize the REST client if the addresses have changed
comment|//happens if there's a failure since we restart the suite cluster due to that
name|Set
argument_list|<
name|InetSocketAddress
argument_list|>
name|newAddresses
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|addresses
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|InetSocketAddress
argument_list|>
name|previousAddresses
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|restClient
operator|.
name|httpAddresses
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|newAddresses
operator|.
name|equals
argument_list|(
name|previousAddresses
argument_list|)
condition|)
block|{
name|restClient
operator|.
name|close
argument_list|()
expr_stmt|;
name|restClient
operator|=
operator|new
name|RestClient
argument_list|(
name|restSpec
argument_list|,
name|settings
argument_list|,
name|addresses
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Clears the last obtained response and the stashed fields      */
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"resetting client, response and stash"
argument_list|)
expr_stmt|;
name|response
operator|=
literal|null
expr_stmt|;
name|stash
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
DECL|method|stash
specifier|public
name|Stash
name|stash
parameter_list|()
block|{
return|return
name|stash
return|;
block|}
comment|/**      * Returns the current es version as a string      */
DECL|method|esVersion
specifier|public
name|String
name|esVersion
parameter_list|()
block|{
return|return
name|restClient
operator|.
name|getEsVersion
argument_list|()
return|;
block|}
comment|/**      * Closes the execution context and releases the underlying resources      */
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|restClient
operator|!=
literal|null
condition|)
block|{
name|restClient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

