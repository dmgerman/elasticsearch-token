begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.cache
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|cache
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
name|unit
operator|.
name|TimeValue
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
name|ESTestCase
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
DECL|class|CacheBuilderTests
specifier|public
class|class
name|CacheBuilderTests
extends|extends
name|ESTestCase
block|{
DECL|method|testSettingExpireAfterAccess
specifier|public
name|void
name|testSettingExpireAfterAccess
parameter_list|()
block|{
name|IllegalArgumentException
name|iae
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|CacheBuilder
operator|.
name|builder
argument_list|()
operator|.
name|setExpireAfterAccess
argument_list|(
name|TimeValue
operator|.
name|MINUS_ONE
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|iae
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"expireAfterAccess<="
argument_list|)
argument_list|)
expr_stmt|;
name|iae
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|CacheBuilder
operator|.
name|builder
argument_list|()
operator|.
name|setExpireAfterAccess
argument_list|(
name|TimeValue
operator|.
name|ZERO
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|iae
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"expireAfterAccess<="
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|TimeValue
name|timeValue
init|=
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|randomPositiveTimeValue
argument_list|()
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|Cache
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|cache
init|=
name|CacheBuilder
operator|.
name|builder
argument_list|()
operator|.
name|setExpireAfterAccess
argument_list|(
name|timeValue
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|timeValue
operator|.
name|getNanos
argument_list|()
argument_list|,
name|cache
operator|.
name|getExpireAfterAccessNanos
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testSettingExpireAfterWrite
specifier|public
name|void
name|testSettingExpireAfterWrite
parameter_list|()
block|{
name|IllegalArgumentException
name|iae
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|CacheBuilder
operator|.
name|builder
argument_list|()
operator|.
name|setExpireAfterWrite
argument_list|(
name|TimeValue
operator|.
name|MINUS_ONE
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|iae
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"expireAfterWrite<="
argument_list|)
argument_list|)
expr_stmt|;
name|iae
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|CacheBuilder
operator|.
name|builder
argument_list|()
operator|.
name|setExpireAfterWrite
argument_list|(
name|TimeValue
operator|.
name|ZERO
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|iae
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"expireAfterWrite<="
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|TimeValue
name|timeValue
init|=
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|randomPositiveTimeValue
argument_list|()
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|Cache
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|cache
init|=
name|CacheBuilder
operator|.
name|builder
argument_list|()
operator|.
name|setExpireAfterWrite
argument_list|(
name|timeValue
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|timeValue
operator|.
name|getNanos
argument_list|()
argument_list|,
name|cache
operator|.
name|getExpireAfterWriteNanos
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

