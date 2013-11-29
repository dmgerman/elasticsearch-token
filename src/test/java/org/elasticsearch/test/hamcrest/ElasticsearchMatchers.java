begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.hamcrest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchHit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Description
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|TypeSafeMatcher
import|;
end_import

begin_class
DECL|class|ElasticsearchMatchers
specifier|public
class|class
name|ElasticsearchMatchers
block|{
DECL|class|SearchHitHasIdMatcher
specifier|public
specifier|static
class|class
name|SearchHitHasIdMatcher
extends|extends
name|TypeSafeMatcher
argument_list|<
name|SearchHit
argument_list|>
block|{
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|method|SearchHitHasIdMatcher
specifier|public
name|SearchHitHasIdMatcher
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|matchesSafely
specifier|protected
name|boolean
name|matchesSafely
parameter_list|(
name|SearchHit
name|searchHit
parameter_list|)
block|{
return|return
name|searchHit
operator|.
name|getId
argument_list|()
operator|.
name|equals
argument_list|(
name|id
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|describeMismatchSafely
specifier|public
name|void
name|describeMismatchSafely
parameter_list|(
specifier|final
name|SearchHit
name|searchHit
parameter_list|,
specifier|final
name|Description
name|mismatchDescription
parameter_list|)
block|{
name|mismatchDescription
operator|.
name|appendText
argument_list|(
literal|" was "
argument_list|)
operator|.
name|appendValue
argument_list|(
name|searchHit
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|describeTo
specifier|public
name|void
name|describeTo
parameter_list|(
specifier|final
name|Description
name|description
parameter_list|)
block|{
name|description
operator|.
name|appendText
argument_list|(
literal|"searchHit id should be "
argument_list|)
operator|.
name|appendValue
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SearchHitHasTypeMatcher
specifier|public
specifier|static
class|class
name|SearchHitHasTypeMatcher
extends|extends
name|TypeSafeMatcher
argument_list|<
name|SearchHit
argument_list|>
block|{
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|method|SearchHitHasTypeMatcher
specifier|public
name|SearchHitHasTypeMatcher
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|matchesSafely
specifier|public
name|boolean
name|matchesSafely
parameter_list|(
specifier|final
name|SearchHit
name|searchHit
parameter_list|)
block|{
return|return
name|searchHit
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
name|type
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|describeMismatchSafely
specifier|public
name|void
name|describeMismatchSafely
parameter_list|(
specifier|final
name|SearchHit
name|searchHit
parameter_list|,
specifier|final
name|Description
name|mismatchDescription
parameter_list|)
block|{
name|mismatchDescription
operator|.
name|appendText
argument_list|(
literal|" was "
argument_list|)
operator|.
name|appendValue
argument_list|(
name|searchHit
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|describeTo
specifier|public
name|void
name|describeTo
parameter_list|(
specifier|final
name|Description
name|description
parameter_list|)
block|{
name|description
operator|.
name|appendText
argument_list|(
literal|"searchHit type should be "
argument_list|)
operator|.
name|appendValue
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SearchHitHasIndexMatcher
specifier|public
specifier|static
class|class
name|SearchHitHasIndexMatcher
extends|extends
name|TypeSafeMatcher
argument_list|<
name|SearchHit
argument_list|>
block|{
DECL|field|index
specifier|private
name|String
name|index
decl_stmt|;
DECL|method|SearchHitHasIndexMatcher
specifier|public
name|SearchHitHasIndexMatcher
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|matchesSafely
specifier|public
name|boolean
name|matchesSafely
parameter_list|(
specifier|final
name|SearchHit
name|searchHit
parameter_list|)
block|{
return|return
name|searchHit
operator|.
name|getIndex
argument_list|()
operator|.
name|equals
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|describeMismatchSafely
specifier|public
name|void
name|describeMismatchSafely
parameter_list|(
specifier|final
name|SearchHit
name|searchHit
parameter_list|,
specifier|final
name|Description
name|mismatchDescription
parameter_list|)
block|{
name|mismatchDescription
operator|.
name|appendText
argument_list|(
literal|" was "
argument_list|)
operator|.
name|appendValue
argument_list|(
name|searchHit
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|describeTo
specifier|public
name|void
name|describeTo
parameter_list|(
specifier|final
name|Description
name|description
parameter_list|)
block|{
name|description
operator|.
name|appendText
argument_list|(
literal|"searchHit index should be "
argument_list|)
operator|.
name|appendValue
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SearchHitHasScoreMatcher
specifier|public
specifier|static
class|class
name|SearchHitHasScoreMatcher
extends|extends
name|TypeSafeMatcher
argument_list|<
name|SearchHit
argument_list|>
block|{
DECL|field|score
specifier|private
name|float
name|score
decl_stmt|;
DECL|method|SearchHitHasScoreMatcher
specifier|public
name|SearchHitHasScoreMatcher
parameter_list|(
name|float
name|score
parameter_list|)
block|{
name|this
operator|.
name|score
operator|=
name|score
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|matchesSafely
specifier|protected
name|boolean
name|matchesSafely
parameter_list|(
name|SearchHit
name|searchHit
parameter_list|)
block|{
return|return
name|searchHit
operator|.
name|getScore
argument_list|()
operator|==
name|score
return|;
block|}
annotation|@
name|Override
DECL|method|describeMismatchSafely
specifier|public
name|void
name|describeMismatchSafely
parameter_list|(
specifier|final
name|SearchHit
name|searchHit
parameter_list|,
specifier|final
name|Description
name|mismatchDescription
parameter_list|)
block|{
name|mismatchDescription
operator|.
name|appendText
argument_list|(
literal|" was "
argument_list|)
operator|.
name|appendValue
argument_list|(
name|searchHit
operator|.
name|getScore
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|describeTo
specifier|public
name|void
name|describeTo
parameter_list|(
specifier|final
name|Description
name|description
parameter_list|)
block|{
name|description
operator|.
name|appendText
argument_list|(
literal|"searchHit score should be "
argument_list|)
operator|.
name|appendValue
argument_list|(
name|score
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

