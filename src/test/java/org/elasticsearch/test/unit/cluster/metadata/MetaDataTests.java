begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.unit.cluster.metadata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|unit
operator|.
name|cluster
operator|.
name|metadata
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
name|Sets
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|IgnoreIndices
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|AliasMetaData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|MetaData
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
name|ImmutableSettings
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
name|IndexMissingException
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
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
operator|.
name|newHashSet
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
name|assertThat
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
name|equalTo
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|MetaDataTests
specifier|public
class|class
name|MetaDataTests
block|{
annotation|@
name|Test
DECL|method|convertWildcardsJustIndicesTests
specifier|public
name|void
name|convertWildcardsJustIndicesTests
parameter_list|()
block|{
name|MetaData
operator|.
name|Builder
name|mdBuilder
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
literal|"testXXX"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
literal|"testXYY"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
literal|"testYYY"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
literal|"kuku"
argument_list|)
argument_list|)
decl_stmt|;
name|MetaData
name|md
init|=
name|mdBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|newHashSet
argument_list|(
name|md
operator|.
name|convertFromWildcards
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"testXXX"
block|}
argument_list|,
literal|true
argument_list|,
name|IgnoreIndices
operator|.
name|MISSING
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newHashSet
argument_list|(
literal|"testXXX"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|newHashSet
argument_list|(
name|md
operator|.
name|convertFromWildcards
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"testXXX"
block|,
literal|"testYYY"
block|}
argument_list|,
literal|true
argument_list|,
name|IgnoreIndices
operator|.
name|MISSING
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newHashSet
argument_list|(
literal|"testXXX"
argument_list|,
literal|"testYYY"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|newHashSet
argument_list|(
name|md
operator|.
name|convertFromWildcards
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"testXXX"
block|,
literal|"ku*"
block|}
argument_list|,
literal|true
argument_list|,
name|IgnoreIndices
operator|.
name|MISSING
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newHashSet
argument_list|(
literal|"testXXX"
argument_list|,
literal|"kuku"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|newHashSet
argument_list|(
name|md
operator|.
name|convertFromWildcards
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"test*"
block|}
argument_list|,
literal|true
argument_list|,
name|IgnoreIndices
operator|.
name|MISSING
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newHashSet
argument_list|(
literal|"testXXX"
argument_list|,
literal|"testXYY"
argument_list|,
literal|"testYYY"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|newHashSet
argument_list|(
name|md
operator|.
name|convertFromWildcards
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"testX*"
block|}
argument_list|,
literal|true
argument_list|,
name|IgnoreIndices
operator|.
name|MISSING
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newHashSet
argument_list|(
literal|"testXXX"
argument_list|,
literal|"testXYY"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|newHashSet
argument_list|(
name|md
operator|.
name|convertFromWildcards
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"testX*"
block|,
literal|"kuku"
block|}
argument_list|,
literal|true
argument_list|,
name|IgnoreIndices
operator|.
name|MISSING
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newHashSet
argument_list|(
literal|"testXXX"
argument_list|,
literal|"testXYY"
argument_list|,
literal|"kuku"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|convertWildcardsTests
specifier|public
name|void
name|convertWildcardsTests
parameter_list|()
block|{
name|MetaData
operator|.
name|Builder
name|mdBuilder
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
literal|"testXXX"
argument_list|)
operator|.
name|putAlias
argument_list|(
name|AliasMetaData
operator|.
name|builder
argument_list|(
literal|"alias1"
argument_list|)
argument_list|)
operator|.
name|putAlias
argument_list|(
name|AliasMetaData
operator|.
name|builder
argument_list|(
literal|"alias2"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
literal|"testXYY"
argument_list|)
operator|.
name|putAlias
argument_list|(
name|AliasMetaData
operator|.
name|builder
argument_list|(
literal|"alias2"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
literal|"testYYY"
argument_list|)
operator|.
name|putAlias
argument_list|(
name|AliasMetaData
operator|.
name|builder
argument_list|(
literal|"alias3"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
literal|"kuku"
argument_list|)
argument_list|)
decl_stmt|;
name|MetaData
name|md
init|=
name|mdBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|newHashSet
argument_list|(
name|md
operator|.
name|convertFromWildcards
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"testYY*"
block|,
literal|"alias*"
block|}
argument_list|,
literal|true
argument_list|,
name|IgnoreIndices
operator|.
name|MISSING
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newHashSet
argument_list|(
literal|"alias1"
argument_list|,
literal|"alias2"
argument_list|,
literal|"alias3"
argument_list|,
literal|"testYYY"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|newHashSet
argument_list|(
name|md
operator|.
name|convertFromWildcards
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-kuku"
block|}
argument_list|,
literal|true
argument_list|,
name|IgnoreIndices
operator|.
name|MISSING
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newHashSet
argument_list|(
literal|"testXXX"
argument_list|,
literal|"testXYY"
argument_list|,
literal|"testYYY"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|newHashSet
argument_list|(
name|md
operator|.
name|convertFromWildcards
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"+test*"
block|,
literal|"-testYYY"
block|}
argument_list|,
literal|true
argument_list|,
name|IgnoreIndices
operator|.
name|MISSING
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newHashSet
argument_list|(
literal|"testXXX"
argument_list|,
literal|"testXYY"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|indexBuilder
specifier|private
name|IndexMetaData
operator|.
name|Builder
name|indexBuilder
parameter_list|(
name|String
name|index
parameter_list|)
block|{
return|return
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|index
argument_list|)
operator|.
name|settings
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Test
argument_list|(
name|expectedExceptions
operator|=
name|IndexMissingException
operator|.
name|class
argument_list|)
DECL|method|concreteIndicesIgnoreIndicesOneMissingIndex
specifier|public
name|void
name|concreteIndicesIgnoreIndicesOneMissingIndex
parameter_list|()
block|{
name|MetaData
operator|.
name|Builder
name|mdBuilder
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
literal|"testXXX"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
literal|"kuku"
argument_list|)
argument_list|)
decl_stmt|;
name|MetaData
name|md
init|=
name|mdBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|md
operator|.
name|concreteIndices
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"testZZZ"
block|}
argument_list|,
name|IgnoreIndices
operator|.
name|MISSING
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|concreteIndicesIgnoreIndicesOneMissingIndexOtherFound
specifier|public
name|void
name|concreteIndicesIgnoreIndicesOneMissingIndexOtherFound
parameter_list|()
block|{
name|MetaData
operator|.
name|Builder
name|mdBuilder
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
literal|"testXXX"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
literal|"kuku"
argument_list|)
argument_list|)
decl_stmt|;
name|MetaData
name|md
init|=
name|mdBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|newHashSet
argument_list|(
name|md
operator|.
name|concreteIndices
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"testXXX"
block|,
literal|"testZZZ"
block|}
argument_list|,
name|IgnoreIndices
operator|.
name|MISSING
argument_list|,
literal|true
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newHashSet
argument_list|(
literal|"testXXX"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expectedExceptions
operator|=
name|IndexMissingException
operator|.
name|class
argument_list|)
DECL|method|concreteIndicesIgnoreIndicesAllMissing
specifier|public
name|void
name|concreteIndicesIgnoreIndicesAllMissing
parameter_list|()
block|{
name|MetaData
operator|.
name|Builder
name|mdBuilder
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
literal|"testXXX"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
literal|"kuku"
argument_list|)
argument_list|)
decl_stmt|;
name|MetaData
name|md
init|=
name|mdBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|newHashSet
argument_list|(
name|md
operator|.
name|concreteIndices
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"testMo"
block|,
literal|"testMahdy"
block|}
argument_list|,
name|IgnoreIndices
operator|.
name|MISSING
argument_list|,
literal|true
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newHashSet
argument_list|(
literal|"testXXX"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|concreteIndicesIgnoreIndicesEmptyRequest
specifier|public
name|void
name|concreteIndicesIgnoreIndicesEmptyRequest
parameter_list|()
block|{
name|MetaData
operator|.
name|Builder
name|mdBuilder
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
literal|"testXXX"
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
literal|"kuku"
argument_list|)
argument_list|)
decl_stmt|;
name|MetaData
name|md
init|=
name|mdBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|newHashSet
argument_list|(
name|md
operator|.
name|concreteIndices
argument_list|(
operator|new
name|String
index|[]
block|{}
argument_list|,
name|IgnoreIndices
operator|.
name|MISSING
argument_list|,
literal|true
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Sets
operator|.
expr|<
name|String
operator|>
name|newHashSet
argument_list|(
literal|"kuku"
argument_list|,
literal|"testXXX"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsAllIndices_null
specifier|public
name|void
name|testIsAllIndices_null
parameter_list|()
throws|throws
name|Exception
block|{
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isAllIndices
argument_list|(
literal|null
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsAllIndices_empty
specifier|public
name|void
name|testIsAllIndices_empty
parameter_list|()
throws|throws
name|Exception
block|{
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isAllIndices
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsAllIndices_explicitAll
specifier|public
name|void
name|testIsAllIndices_explicitAll
parameter_list|()
throws|throws
name|Exception
block|{
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isAllIndices
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"_all"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsAllIndices_explicitAllPlusOther
specifier|public
name|void
name|testIsAllIndices_explicitAllPlusOther
parameter_list|()
throws|throws
name|Exception
block|{
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isAllIndices
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"_all"
block|,
literal|"other"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsAllIndices_normalIndexes
specifier|public
name|void
name|testIsAllIndices_normalIndexes
parameter_list|()
throws|throws
name|Exception
block|{
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isAllIndices
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"index1"
block|,
literal|"index2"
block|,
literal|"index3"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsAllIndices_wildcard
specifier|public
name|void
name|testIsAllIndices_wildcard
parameter_list|()
throws|throws
name|Exception
block|{
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isAllIndices
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"*"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsExplicitAllIndices_null
specifier|public
name|void
name|testIsExplicitAllIndices_null
parameter_list|()
throws|throws
name|Exception
block|{
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isExplicitAllIndices
argument_list|(
literal|null
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsExplicitAllIndices_empty
specifier|public
name|void
name|testIsExplicitAllIndices_empty
parameter_list|()
throws|throws
name|Exception
block|{
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isExplicitAllIndices
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsExplicitAllIndices_explicitAll
specifier|public
name|void
name|testIsExplicitAllIndices_explicitAll
parameter_list|()
throws|throws
name|Exception
block|{
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isExplicitAllIndices
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"_all"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsExplicitAllIndices_explicitAllPlusOther
specifier|public
name|void
name|testIsExplicitAllIndices_explicitAllPlusOther
parameter_list|()
throws|throws
name|Exception
block|{
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isExplicitAllIndices
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"_all"
block|,
literal|"other"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsExplicitAllIndices_normalIndexes
specifier|public
name|void
name|testIsExplicitAllIndices_normalIndexes
parameter_list|()
throws|throws
name|Exception
block|{
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isExplicitAllIndices
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"index1"
block|,
literal|"index2"
block|,
literal|"index3"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsExplicitAllIndices_wildcard
specifier|public
name|void
name|testIsExplicitAllIndices_wildcard
parameter_list|()
throws|throws
name|Exception
block|{
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isExplicitAllIndices
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"*"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsPatternMatchingAllIndices_explicitList
specifier|public
name|void
name|testIsPatternMatchingAllIndices_explicitList
parameter_list|()
throws|throws
name|Exception
block|{
comment|//even though it does identify all indices, it's not a pattern but just an explicit list of them
name|String
index|[]
name|concreteIndices
init|=
operator|new
name|String
index|[]
block|{
literal|"index1"
block|,
literal|"index2"
block|,
literal|"index3"
block|}
decl_stmt|;
name|String
index|[]
name|indicesOrAliases
init|=
name|concreteIndices
decl_stmt|;
name|String
index|[]
name|allConcreteIndices
init|=
name|concreteIndices
decl_stmt|;
name|MetaData
name|metaData
init|=
name|metaDataBuilder
argument_list|(
name|allConcreteIndices
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isPatternMatchingAllIndices
argument_list|(
name|indicesOrAliases
argument_list|,
name|concreteIndices
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsPatternMatchingAllIndices_onlyWildcard
specifier|public
name|void
name|testIsPatternMatchingAllIndices_onlyWildcard
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|indicesOrAliases
init|=
operator|new
name|String
index|[]
block|{
literal|"*"
block|}
decl_stmt|;
name|String
index|[]
name|concreteIndices
init|=
operator|new
name|String
index|[]
block|{
literal|"index1"
block|,
literal|"index2"
block|,
literal|"index3"
block|}
decl_stmt|;
name|String
index|[]
name|allConcreteIndices
init|=
name|concreteIndices
decl_stmt|;
name|MetaData
name|metaData
init|=
name|metaDataBuilder
argument_list|(
name|allConcreteIndices
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isPatternMatchingAllIndices
argument_list|(
name|indicesOrAliases
argument_list|,
name|concreteIndices
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsPatternMatchingAllIndices_matchingTrailingWildcard
specifier|public
name|void
name|testIsPatternMatchingAllIndices_matchingTrailingWildcard
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|indicesOrAliases
init|=
operator|new
name|String
index|[]
block|{
literal|"index*"
block|}
decl_stmt|;
name|String
index|[]
name|concreteIndices
init|=
operator|new
name|String
index|[]
block|{
literal|"index1"
block|,
literal|"index2"
block|,
literal|"index3"
block|}
decl_stmt|;
name|String
index|[]
name|allConcreteIndices
init|=
name|concreteIndices
decl_stmt|;
name|MetaData
name|metaData
init|=
name|metaDataBuilder
argument_list|(
name|allConcreteIndices
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isPatternMatchingAllIndices
argument_list|(
name|indicesOrAliases
argument_list|,
name|concreteIndices
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsPatternMatchingAllIndices_nonMatchingTrailingWildcard
specifier|public
name|void
name|testIsPatternMatchingAllIndices_nonMatchingTrailingWildcard
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|indicesOrAliases
init|=
operator|new
name|String
index|[]
block|{
literal|"index*"
block|}
decl_stmt|;
name|String
index|[]
name|concreteIndices
init|=
operator|new
name|String
index|[]
block|{
literal|"index1"
block|,
literal|"index2"
block|,
literal|"index3"
block|}
decl_stmt|;
name|String
index|[]
name|allConcreteIndices
init|=
operator|new
name|String
index|[]
block|{
literal|"index1"
block|,
literal|"index2"
block|,
literal|"index3"
block|,
literal|"a"
block|,
literal|"b"
block|}
decl_stmt|;
name|MetaData
name|metaData
init|=
name|metaDataBuilder
argument_list|(
name|allConcreteIndices
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isPatternMatchingAllIndices
argument_list|(
name|indicesOrAliases
argument_list|,
name|concreteIndices
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsPatternMatchingAllIndices_matchingSingleExclusion
specifier|public
name|void
name|testIsPatternMatchingAllIndices_matchingSingleExclusion
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|indicesOrAliases
init|=
operator|new
name|String
index|[]
block|{
literal|"-index1"
block|,
literal|"+index1"
block|}
decl_stmt|;
name|String
index|[]
name|concreteIndices
init|=
operator|new
name|String
index|[]
block|{
literal|"index1"
block|,
literal|"index2"
block|,
literal|"index3"
block|}
decl_stmt|;
name|String
index|[]
name|allConcreteIndices
init|=
name|concreteIndices
decl_stmt|;
name|MetaData
name|metaData
init|=
name|metaDataBuilder
argument_list|(
name|allConcreteIndices
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isPatternMatchingAllIndices
argument_list|(
name|indicesOrAliases
argument_list|,
name|concreteIndices
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsPatternMatchingAllIndices_nonMatchingSingleExclusion
specifier|public
name|void
name|testIsPatternMatchingAllIndices_nonMatchingSingleExclusion
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|indicesOrAliases
init|=
operator|new
name|String
index|[]
block|{
literal|"-index1"
block|}
decl_stmt|;
name|String
index|[]
name|concreteIndices
init|=
operator|new
name|String
index|[]
block|{
literal|"index2"
block|,
literal|"index3"
block|}
decl_stmt|;
name|String
index|[]
name|allConcreteIndices
init|=
operator|new
name|String
index|[]
block|{
literal|"index1"
block|,
literal|"index2"
block|,
literal|"index3"
block|}
decl_stmt|;
name|MetaData
name|metaData
init|=
name|metaDataBuilder
argument_list|(
name|allConcreteIndices
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isPatternMatchingAllIndices
argument_list|(
name|indicesOrAliases
argument_list|,
name|concreteIndices
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsPatternMatchingAllIndices_matchingTrailingWildcardAndExclusion
specifier|public
name|void
name|testIsPatternMatchingAllIndices_matchingTrailingWildcardAndExclusion
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|indicesOrAliases
init|=
operator|new
name|String
index|[]
block|{
literal|"index*"
block|,
literal|"-index1"
block|,
literal|"+index1"
block|}
decl_stmt|;
name|String
index|[]
name|concreteIndices
init|=
operator|new
name|String
index|[]
block|{
literal|"index1"
block|,
literal|"index2"
block|,
literal|"index3"
block|}
decl_stmt|;
name|String
index|[]
name|allConcreteIndices
init|=
name|concreteIndices
decl_stmt|;
name|MetaData
name|metaData
init|=
name|metaDataBuilder
argument_list|(
name|allConcreteIndices
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isPatternMatchingAllIndices
argument_list|(
name|indicesOrAliases
argument_list|,
name|concreteIndices
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsPatternMatchingAllIndices_nonMatchingTrailingWildcardAndExclusion
specifier|public
name|void
name|testIsPatternMatchingAllIndices_nonMatchingTrailingWildcardAndExclusion
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|indicesOrAliases
init|=
operator|new
name|String
index|[]
block|{
literal|"index*"
block|,
literal|"-index1"
block|}
decl_stmt|;
name|String
index|[]
name|concreteIndices
init|=
operator|new
name|String
index|[]
block|{
literal|"index2"
block|,
literal|"index3"
block|}
decl_stmt|;
name|String
index|[]
name|allConcreteIndices
init|=
operator|new
name|String
index|[]
block|{
literal|"index1"
block|,
literal|"index2"
block|,
literal|"index3"
block|}
decl_stmt|;
name|MetaData
name|metaData
init|=
name|metaDataBuilder
argument_list|(
name|allConcreteIndices
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|metaData
operator|.
name|isPatternMatchingAllIndices
argument_list|(
name|indicesOrAliases
argument_list|,
name|concreteIndices
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|metaDataBuilder
specifier|private
name|MetaData
name|metaDataBuilder
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|MetaData
operator|.
name|Builder
name|mdBuilder
init|=
name|MetaData
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|concreteIndex
range|:
name|indices
control|)
block|{
name|mdBuilder
operator|.
name|put
argument_list|(
name|indexBuilder
argument_list|(
name|concreteIndex
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|mdBuilder
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

