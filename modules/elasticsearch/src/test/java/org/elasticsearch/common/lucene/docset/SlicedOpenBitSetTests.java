begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.docset
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|docset
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|DocIdSetIterator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|OpenBitSet
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
name|io
operator|.
name|IOException
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

begin_class
annotation|@
name|Test
DECL|class|SlicedOpenBitSetTests
specifier|public
class|class
name|SlicedOpenBitSetTests
block|{
DECL|method|simpleTests
annotation|@
name|Test
specifier|public
name|void
name|simpleTests
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|numberOfBits
init|=
literal|500
decl_stmt|;
name|SlicedOpenBitSet
name|bitSet
init|=
operator|new
name|SlicedOpenBitSet
argument_list|(
operator|new
name|long
index|[
name|OpenBitSet
operator|.
name|bits2words
argument_list|(
name|numberOfBits
argument_list|)
operator|+
literal|100
index|]
argument_list|,
name|OpenBitSet
operator|.
name|bits2words
argument_list|(
name|numberOfBits
argument_list|)
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|bitSet
operator|.
name|fastSet
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bitSet
operator|.
name|fastGet
argument_list|(
literal|100
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|DocIdSetIterator
name|iterator
init|=
name|bitSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|iterator
operator|.
name|nextDoc
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|100
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|iterator
operator|.
name|nextDoc
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

