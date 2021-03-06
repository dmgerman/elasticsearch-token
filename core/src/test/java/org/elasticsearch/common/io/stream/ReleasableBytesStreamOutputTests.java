begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.io.stream
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|stream
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

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|MockBigArrays
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|breaker
operator|.
name|NoneCircuitBreakerService
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
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
DECL|class|ReleasableBytesStreamOutputTests
specifier|public
class|class
name|ReleasableBytesStreamOutputTests
extends|extends
name|ESTestCase
block|{
DECL|method|testRelease
specifier|public
name|void
name|testRelease
parameter_list|()
throws|throws
name|Exception
block|{
name|MockBigArrays
name|mockBigArrays
init|=
operator|new
name|MockBigArrays
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
operator|new
name|NoneCircuitBreakerService
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|ReleasableBytesStreamOutput
name|output
init|=
name|getRandomReleasableBytesStreamOutput
argument_list|(
name|mockBigArrays
argument_list|)
init|)
block|{
name|output
operator|.
name|writeBoolean
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|MockBigArrays
operator|.
name|ensureAllArraysAreReleased
argument_list|()
expr_stmt|;
block|}
DECL|method|getRandomReleasableBytesStreamOutput
specifier|private
name|ReleasableBytesStreamOutput
name|getRandomReleasableBytesStreamOutput
parameter_list|(
name|MockBigArrays
name|mockBigArrays
parameter_list|)
throws|throws
name|IOException
block|{
name|ReleasableBytesStreamOutput
name|output
init|=
operator|new
name|ReleasableBytesStreamOutput
argument_list|(
name|mockBigArrays
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|scaledRandomIntBetween
argument_list|(
literal|1
argument_list|,
literal|32
argument_list|)
condition|;
name|i
operator|++
control|)
block|{
name|output
operator|.
name|write
argument_list|(
name|randomByte
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|output
return|;
block|}
block|}
end_class

end_unit

