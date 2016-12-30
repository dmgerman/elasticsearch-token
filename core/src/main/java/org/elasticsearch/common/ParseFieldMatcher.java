begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
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
name|settings
operator|.
name|Settings
import|;
end_import

begin_comment
comment|/**  * Matcher to use in combination with {@link ParseField} while parsing requests.  *  * @deprecated This class used to be useful to parse in strict mode and emit errors rather than deprecation warnings. Now that we return  * warnings as response headers all the time, it is no longer useful and will soon be removed. The removal is in progress and there is  * already no strict mode in fact. Use {@link ParseField} directly.  */
end_comment

begin_class
annotation|@
name|Deprecated
DECL|class|ParseFieldMatcher
specifier|public
class|class
name|ParseFieldMatcher
block|{
DECL|field|EMPTY
specifier|public
specifier|static
specifier|final
name|ParseFieldMatcher
name|EMPTY
init|=
operator|new
name|ParseFieldMatcher
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
DECL|field|STRICT
specifier|public
specifier|static
specifier|final
name|ParseFieldMatcher
name|STRICT
init|=
operator|new
name|ParseFieldMatcher
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
DECL|method|ParseFieldMatcher
specifier|public
name|ParseFieldMatcher
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
comment|//we don't do anything with the settings argument, this whole class will be soon removed
block|}
block|}
end_class

end_unit

