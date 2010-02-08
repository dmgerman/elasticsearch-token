begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.engine
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|engine
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
name|MatchAllDocsQuery
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
name|search
operator|.
name|Query
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|Lucene
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
name|Matcher
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * @author kimchy  */
end_comment

begin_class
DECL|class|EngineSearcherTotalHitsMatcher
specifier|public
specifier|final
class|class
name|EngineSearcherTotalHitsMatcher
extends|extends
name|TypeSafeMatcher
argument_list|<
name|Engine
operator|.
name|Searcher
argument_list|>
block|{
DECL|field|query
specifier|private
specifier|final
name|Query
name|query
decl_stmt|;
DECL|field|totalHits
specifier|private
specifier|final
name|int
name|totalHits
decl_stmt|;
DECL|method|EngineSearcherTotalHitsMatcher
specifier|public
name|EngineSearcherTotalHitsMatcher
parameter_list|(
name|Query
name|query
parameter_list|,
name|int
name|totalHits
parameter_list|)
block|{
name|this
operator|.
name|query
operator|=
name|query
expr_stmt|;
name|this
operator|.
name|totalHits
operator|=
name|totalHits
expr_stmt|;
block|}
DECL|method|matchesSafely
annotation|@
name|Override
specifier|public
name|boolean
name|matchesSafely
parameter_list|(
name|Engine
operator|.
name|Searcher
name|searcher
parameter_list|)
block|{
try|try
block|{
name|long
name|count
init|=
name|Lucene
operator|.
name|count
argument_list|(
name|searcher
operator|.
name|searcher
argument_list|()
argument_list|,
name|query
argument_list|,
operator|-
literal|1f
argument_list|)
decl_stmt|;
return|return
name|count
operator|==
name|totalHits
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
DECL|method|describeTo
annotation|@
name|Override
specifier|public
name|void
name|describeTo
parameter_list|(
name|Description
name|description
parameter_list|)
block|{
name|description
operator|.
name|appendText
argument_list|(
literal|"total hits of size "
argument_list|)
operator|.
name|appendValue
argument_list|(
name|totalHits
argument_list|)
operator|.
name|appendText
argument_list|(
literal|" with query "
argument_list|)
operator|.
name|appendValue
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
DECL|method|engineSearcherTotalHits
specifier|public
specifier|static
name|Matcher
argument_list|<
name|Engine
operator|.
name|Searcher
argument_list|>
name|engineSearcherTotalHits
parameter_list|(
name|Query
name|query
parameter_list|,
name|int
name|totalHits
parameter_list|)
block|{
return|return
operator|new
name|EngineSearcherTotalHitsMatcher
argument_list|(
name|query
argument_list|,
name|totalHits
argument_list|)
return|;
block|}
DECL|method|engineSearcherTotalHits
specifier|public
specifier|static
name|Matcher
argument_list|<
name|Engine
operator|.
name|Searcher
argument_list|>
name|engineSearcherTotalHits
parameter_list|(
name|int
name|totalHits
parameter_list|)
block|{
return|return
operator|new
name|EngineSearcherTotalHitsMatcher
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|totalHits
argument_list|)
return|;
block|}
block|}
end_class

end_unit

