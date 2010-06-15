begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|thread
operator|.
name|ThreadLocals
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|*
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
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
annotation|@
name|Test
DECL|class|ThreadLocalsTests
specifier|public
class|class
name|ThreadLocalsTests
block|{
DECL|field|local
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|AtomicInteger
argument_list|>
argument_list|>
name|local
init|=
operator|new
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|AtomicInteger
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|AtomicInteger
argument_list|>
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|AtomicInteger
argument_list|>
argument_list|(
operator|new
name|AtomicInteger
argument_list|()
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|method|testCleanThreadLocals
annotation|@
name|Test
specifier|public
name|void
name|testCleanThreadLocals
parameter_list|()
block|{
name|assertThat
argument_list|(
name|local
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|local
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|local
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|ThreadLocals
operator|.
name|clearReferencesThreadLocals
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|local
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|ThreadLocals
operator|.
name|clearReferencesThreadLocals
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

