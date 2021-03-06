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
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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

begin_class
DECL|class|UUIDTests
specifier|public
class|class
name|UUIDTests
extends|extends
name|ESTestCase
block|{
DECL|field|timeUUIDGen
specifier|static
name|UUIDGenerator
name|timeUUIDGen
init|=
operator|new
name|TimeBasedUUIDGenerator
argument_list|()
decl_stmt|;
DECL|field|randomUUIDGen
specifier|static
name|UUIDGenerator
name|randomUUIDGen
init|=
operator|new
name|RandomBasedUUIDGenerator
argument_list|()
decl_stmt|;
DECL|method|testRandomUUID
specifier|public
name|void
name|testRandomUUID
parameter_list|()
block|{
name|verifyUUIDSet
argument_list|(
literal|100000
argument_list|,
name|randomUUIDGen
argument_list|)
expr_stmt|;
block|}
DECL|method|testTimeUUID
specifier|public
name|void
name|testTimeUUID
parameter_list|()
block|{
name|verifyUUIDSet
argument_list|(
literal|100000
argument_list|,
name|timeUUIDGen
argument_list|)
expr_stmt|;
block|}
DECL|method|testThreadedTimeUUID
specifier|public
name|void
name|testThreadedTimeUUID
parameter_list|()
block|{
name|testUUIDThreaded
argument_list|(
name|timeUUIDGen
argument_list|)
expr_stmt|;
block|}
DECL|method|testThreadedRandomUUID
specifier|public
name|void
name|testThreadedRandomUUID
parameter_list|()
block|{
name|testUUIDThreaded
argument_list|(
name|randomUUIDGen
argument_list|)
expr_stmt|;
block|}
DECL|method|verifyUUIDSet
name|Set
argument_list|<
name|String
argument_list|>
name|verifyUUIDSet
parameter_list|(
name|int
name|count
parameter_list|,
name|UUIDGenerator
name|uuidSource
parameter_list|)
block|{
name|HashSet
argument_list|<
name|String
argument_list|>
name|uuidSet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
condition|;
operator|++
name|i
control|)
block|{
name|uuidSet
operator|.
name|add
argument_list|(
name|uuidSource
operator|.
name|getBase64UUID
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|count
argument_list|,
name|uuidSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|uuidSet
return|;
block|}
DECL|class|UUIDGenRunner
class|class
name|UUIDGenRunner
implements|implements
name|Runnable
block|{
DECL|field|count
name|int
name|count
decl_stmt|;
DECL|field|uuidSet
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|uuidSet
init|=
literal|null
decl_stmt|;
DECL|field|uuidSource
name|UUIDGenerator
name|uuidSource
decl_stmt|;
DECL|method|UUIDGenRunner
name|UUIDGenRunner
parameter_list|(
name|int
name|count
parameter_list|,
name|UUIDGenerator
name|uuidSource
parameter_list|)
block|{
name|this
operator|.
name|count
operator|=
name|count
expr_stmt|;
name|this
operator|.
name|uuidSource
operator|=
name|uuidSource
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
name|uuidSet
operator|=
name|verifyUUIDSet
argument_list|(
name|count
argument_list|,
name|uuidSource
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testUUIDThreaded
specifier|public
name|void
name|testUUIDThreaded
parameter_list|(
name|UUIDGenerator
name|uuidSource
parameter_list|)
block|{
name|HashSet
argument_list|<
name|UUIDGenRunner
argument_list|>
name|runners
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|HashSet
argument_list|<
name|Thread
argument_list|>
name|threads
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|count
init|=
literal|20
decl_stmt|;
name|int
name|uuids
init|=
literal|10000
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
condition|;
operator|++
name|i
control|)
block|{
name|UUIDGenRunner
name|runner
init|=
operator|new
name|UUIDGenRunner
argument_list|(
name|uuids
argument_list|,
name|uuidSource
argument_list|)
decl_stmt|;
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
name|runner
argument_list|)
decl_stmt|;
name|threads
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|runners
operator|.
name|add
argument_list|(
name|runner
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Thread
name|t
range|:
name|threads
control|)
block|{
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|boolean
name|retry
init|=
literal|false
decl_stmt|;
do|do
block|{
for|for
control|(
name|Thread
name|t
range|:
name|threads
control|)
block|{
try|try
block|{
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|retry
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
do|while
condition|(
name|retry
condition|)
do|;
name|HashSet
argument_list|<
name|String
argument_list|>
name|globalSet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|UUIDGenRunner
name|runner
range|:
name|runners
control|)
block|{
name|globalSet
operator|.
name|addAll
argument_list|(
name|runner
operator|.
name|uuidSet
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|count
operator|*
name|uuids
argument_list|,
name|globalSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

