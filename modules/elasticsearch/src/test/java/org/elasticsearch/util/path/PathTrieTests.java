begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.path
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|path
package|;
end_package

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
name|Map
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|PathTrieTests
specifier|public
class|class
name|PathTrieTests
block|{
DECL|method|testPath
annotation|@
name|Test
specifier|public
name|void
name|testPath
parameter_list|()
block|{
name|PathTrie
argument_list|<
name|String
argument_list|>
name|trie
init|=
operator|new
name|PathTrie
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|trie
operator|.
name|insert
argument_list|(
literal|"/a/b/c"
argument_list|,
literal|"walla"
argument_list|)
expr_stmt|;
name|trie
operator|.
name|insert
argument_list|(
literal|"a/d/g"
argument_list|,
literal|"kuku"
argument_list|)
expr_stmt|;
name|trie
operator|.
name|insert
argument_list|(
literal|"x/b/c"
argument_list|,
literal|"lala"
argument_list|)
expr_stmt|;
name|trie
operator|.
name|insert
argument_list|(
literal|"a/x/*"
argument_list|,
literal|"one"
argument_list|)
expr_stmt|;
name|trie
operator|.
name|insert
argument_list|(
literal|"a/b/*"
argument_list|,
literal|"two"
argument_list|)
expr_stmt|;
name|trie
operator|.
name|insert
argument_list|(
literal|"*/*/x"
argument_list|,
literal|"three"
argument_list|)
expr_stmt|;
name|trie
operator|.
name|insert
argument_list|(
literal|"{index}/insert/{docId}"
argument_list|,
literal|"bingo"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|trie
operator|.
name|retrieve
argument_list|(
literal|"a/b/c"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"walla"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|trie
operator|.
name|retrieve
argument_list|(
literal|"a/d/g"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"kuku"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|trie
operator|.
name|retrieve
argument_list|(
literal|"x/b/c"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"lala"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|trie
operator|.
name|retrieve
argument_list|(
literal|"a/x/b"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"one"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|trie
operator|.
name|retrieve
argument_list|(
literal|"a/b/d"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"two"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|trie
operator|.
name|retrieve
argument_list|(
literal|"a/b"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|trie
operator|.
name|retrieve
argument_list|(
literal|"a/b/c/d"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|trie
operator|.
name|retrieve
argument_list|(
literal|"g/t/x"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"three"
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
name|newHashMap
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|trie
operator|.
name|retrieve
argument_list|(
literal|"index1/insert/12"
argument_list|,
name|params
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"bingo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|params
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|params
operator|.
name|get
argument_list|(
literal|"index"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"index1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|params
operator|.
name|get
argument_list|(
literal|"docId"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"12"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testEmptyPath
annotation|@
name|Test
specifier|public
name|void
name|testEmptyPath
parameter_list|()
block|{
name|PathTrie
argument_list|<
name|String
argument_list|>
name|trie
init|=
operator|new
name|PathTrie
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|trie
operator|.
name|insert
argument_list|(
literal|"/"
argument_list|,
literal|"walla"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|trie
operator|.
name|retrieve
argument_list|(
literal|""
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"walla"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

