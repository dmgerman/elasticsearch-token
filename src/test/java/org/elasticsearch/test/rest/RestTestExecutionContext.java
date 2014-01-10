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
DECL|field|restClient
specifier|private
specifier|final
name|RestClient
name|restClient
decl_stmt|;
DECL|field|esVersion
specifier|private
specifier|final
name|String
name|esVersion
decl_stmt|;
DECL|field|stash
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|stash
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
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
name|String
name|host
parameter_list|,
name|int
name|port
parameter_list|,
name|RestSpec
name|restSpec
parameter_list|)
throws|throws
name|RestException
throws|,
name|IOException
block|{
name|this
operator|.
name|restClient
operator|=
operator|new
name|RestClient
argument_list|(
name|host
argument_list|,
name|port
argument_list|,
name|restSpec
argument_list|)
expr_stmt|;
name|this
operator|.
name|esVersion
operator|=
name|restClient
operator|.
name|getEsVersion
argument_list|()
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
name|String
name|body
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
name|isStashed
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
name|unstash
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
try|try
block|{
return|return
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
comment|/**      * Calls an elasticsearch api internally without saving the obtained response in the context.      * Useful for internal calls (e.g. delete index during teardown)      * @throws RestException if the returned status code is non ok      */
DECL|method|callApiInternal
specifier|public
name|RestResponse
name|callApiInternal
parameter_list|(
name|String
name|apiName
parameter_list|,
name|String
modifier|...
name|params
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
argument_list|)
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
argument_list|)
return|;
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
literal|"resetting response and stash"
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
comment|/**      * Tells whether a particular value needs to be looked up in the stash      * The stash contains fields eventually extracted from previous responses that can be reused      * as arguments for following requests (e.g. scroll_id)      */
DECL|method|isStashed
specifier|public
name|boolean
name|isStashed
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|String
name|stashKey
init|=
name|key
operator|.
name|toString
argument_list|()
decl_stmt|;
return|return
name|Strings
operator|.
name|hasLength
argument_list|(
name|stashKey
argument_list|)
operator|&&
name|stashKey
operator|.
name|startsWith
argument_list|(
literal|"$"
argument_list|)
return|;
block|}
comment|/**      * Extracts a value from the current stash      * The stash contains fields eventually extracted from previous responses that can be reused      * as arguments for following requests (e.g. scroll_id)      */
DECL|method|unstash
specifier|public
name|Object
name|unstash
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|Object
name|stashedValue
init|=
name|stash
operator|.
name|get
argument_list|(
name|value
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|stashedValue
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"stashed value not found for key ["
operator|+
name|value
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|stashedValue
return|;
block|}
comment|/**      * Allows to saved a specific field in the stash as key-value pair      */
DECL|method|stash
specifier|public
name|void
name|stash
parameter_list|(
name|String
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"stashing [{}]=[{}]"
argument_list|,
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|Object
name|old
init|=
name|stash
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|old
operator|!=
literal|null
operator|&&
name|old
operator|!=
name|value
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"replaced stashed value [{}] with same key [{}]"
argument_list|,
name|old
argument_list|,
name|key
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Returns the current es version as a string      */
DECL|method|esVersion
specifier|public
name|String
name|esVersion
parameter_list|()
block|{
return|return
name|esVersion
return|;
block|}
comment|/**      * Closes the execution context and releases the underlying resources      */
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|this
operator|.
name|restClient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

