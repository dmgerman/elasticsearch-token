begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.reindex
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|reindex
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|containsString
import|;
end_import

begin_class
DECL|class|UpdateByQueryWithScriptTests
specifier|public
class|class
name|UpdateByQueryWithScriptTests
extends|extends
name|AbstractAsyncBulkIndexByScrollActionScriptTestCase
argument_list|<
name|UpdateByQueryRequest
argument_list|,
name|BulkIndexByScrollResponse
argument_list|>
block|{
DECL|method|testModifyingCtxNotAllowed
specifier|public
name|void
name|testModifyingCtxNotAllowed
parameter_list|()
block|{
comment|/*          * Its important that none of these actually match any of the fields.          * They don't now, but make sure they still don't match if you add any          * more. The point of have many is that they should all present the same          * error message to the user, not some ClassCastException.          */
name|Object
index|[]
name|options
init|=
operator|new
name|Object
index|[]
block|{
literal|"cat"
block|,
operator|new
name|Object
argument_list|()
block|,
literal|123
block|,
operator|new
name|Date
argument_list|()
block|,
name|Math
operator|.
name|PI
block|}
decl_stmt|;
for|for
control|(
name|String
name|ctxVar
range|:
operator|new
name|String
index|[]
block|{
literal|"_index"
block|,
literal|"_type"
block|,
literal|"_id"
block|,
literal|"_version"
block|,
literal|"_parent"
block|,
literal|"_routing"
block|,
literal|"_timestamp"
block|,
literal|"_ttl"
block|}
control|)
block|{
try|try
block|{
name|applyScript
argument_list|(
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|ctx
parameter_list|)
lambda|->
name|ctx
operator|.
name|put
argument_list|(
name|ctxVar
argument_list|,
name|randomFrom
argument_list|(
name|options
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Modifying ["
operator|+
name|ctxVar
operator|+
literal|"] not allowed"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|request
specifier|protected
name|UpdateByQueryRequest
name|request
parameter_list|()
block|{
return|return
operator|new
name|UpdateByQueryRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|action
specifier|protected
name|AbstractAsyncBulkIndexByScrollAction
argument_list|<
name|UpdateByQueryRequest
argument_list|,
name|BulkIndexByScrollResponse
argument_list|>
name|action
parameter_list|()
block|{
return|return
operator|new
name|TransportUpdateByQueryAction
operator|.
name|AsyncIndexBySearchAction
argument_list|(
name|logger
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|threadPool
argument_list|,
name|request
argument_list|()
argument_list|,
name|listener
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

