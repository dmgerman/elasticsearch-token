begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|index
operator|.
name|Term
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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|SynonymQuery
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
name|BooleanQuery
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
name|BooleanClause
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
name|TermQuery
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
name|PhraseQuery
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
name|DisjunctionMaxQuery
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
name|MultiPhraseQuery
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
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|IndexService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|search
operator|.
name|MatchQuery
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
name|ESSingleNodeTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|Matchers
operator|.
name|equalTo
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
name|instanceOf
import|;
end_import

begin_comment
comment|/**  * Makes sure that graph analysis is disabled with shingle filters of different size  */
end_comment

begin_class
DECL|class|DisableGraphQueryTests
specifier|public
class|class
name|DisableGraphQueryTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|field|indexService
specifier|private
specifier|static
name|IndexService
name|indexService
decl_stmt|;
DECL|field|shardContext
specifier|private
specifier|static
name|QueryShardContext
name|shardContext
decl_stmt|;
DECL|field|expectedQuery
specifier|private
specifier|static
name|Query
name|expectedQuery
decl_stmt|;
DECL|field|expectedPhraseQuery
specifier|private
specifier|static
name|Query
name|expectedPhraseQuery
decl_stmt|;
DECL|field|expectedQueryWithUnigram
specifier|private
specifier|static
name|Query
name|expectedQueryWithUnigram
decl_stmt|;
DECL|field|expectedPhraseQueryWithUnigram
specifier|private
specifier|static
name|Query
name|expectedPhraseQueryWithUnigram
decl_stmt|;
annotation|@
name|Before
DECL|method|setup
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.shingle.type"
argument_list|,
literal|"shingle"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.shingle.output_unigrams"
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.shingle.min_size"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.shingle.max_size"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.shingle_unigram.type"
argument_list|,
literal|"shingle"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.shingle_unigram.output_unigrams"
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.shingle_unigram.min_size"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.shingle_unigram.max_size"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.analyzer.text_shingle.tokenizer"
argument_list|,
literal|"whitespace"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.analyzer.text_shingle.filter"
argument_list|,
literal|"lowercase, shingle"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.analyzer.text_shingle_unigram.tokenizer"
argument_list|,
literal|"whitespace"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.analyzer.text_shingle_unigram.filter"
argument_list|,
literal|"lowercase, shingle_unigram"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|indexService
operator|=
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|settings
argument_list|,
literal|"t"
argument_list|,
literal|"text_shingle"
argument_list|,
literal|"type=text,analyzer=text_shingle"
argument_list|,
literal|"text_shingle_unigram"
argument_list|,
literal|"type=text,analyzer=text_shingle_unigram"
argument_list|)
expr_stmt|;
name|shardContext
operator|=
name|indexService
operator|.
name|newQueryShardContext
argument_list|(
literal|0
argument_list|,
literal|null
argument_list|,
parameter_list|()
lambda|->
literal|0L
argument_list|)
expr_stmt|;
comment|// parsed queries for "text_shingle_unigram:(foo bar baz)" with query parsers
comment|// that ignores position length attribute
name|expectedQueryWithUnigram
operator|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|SynonymQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text_shingle_unigram"
argument_list|,
literal|"foo"
argument_list|)
argument_list|,
operator|new
name|Term
argument_list|(
literal|"text_shingle_unigram"
argument_list|,
literal|"foo bar"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|SynonymQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text_shingle_unigram"
argument_list|,
literal|"bar"
argument_list|)
argument_list|,
operator|new
name|Term
argument_list|(
literal|"text_shingle_unigram"
argument_list|,
literal|"bar baz"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text_shingle_unigram"
argument_list|,
literal|"baz"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
comment|// parsed query for "text_shingle_unigram:\"foo bar baz\" with query parsers
comment|// that ignores position length attribute
name|expectedPhraseQueryWithUnigram
operator|=
operator|new
name|MultiPhraseQuery
operator|.
name|Builder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|Term
index|[]
block|{
operator|new
name|Term
argument_list|(
literal|"text_shingle_unigram"
argument_list|,
literal|"foo"
argument_list|)
block|,
operator|new
name|Term
argument_list|(
literal|"text_shingle_unigram"
argument_list|,
literal|"foo bar"
argument_list|)
block|}
argument_list|,
literal|0
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|Term
index|[]
block|{
operator|new
name|Term
argument_list|(
literal|"text_shingle_unigram"
argument_list|,
literal|"bar"
argument_list|)
block|,
operator|new
name|Term
argument_list|(
literal|"text_shingle_unigram"
argument_list|,
literal|"bar baz"
argument_list|)
block|}
argument_list|,
literal|1
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|Term
index|[]
block|{
operator|new
name|Term
argument_list|(
literal|"text_shingle_unigram"
argument_list|,
literal|"baz"
argument_list|)
block|,                 }
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
comment|// parsed query for "text_shingle:(foo bar baz)
name|expectedQuery
operator|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text_shingle"
argument_list|,
literal|"foo bar"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text_shingle"
argument_list|,
literal|"bar baz"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text_shingle"
argument_list|,
literal|"baz biz"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
comment|// parsed query for "text_shingle:"foo bar baz"
name|expectedPhraseQuery
operator|=
operator|new
name|PhraseQuery
operator|.
name|Builder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text_shingle"
argument_list|,
literal|"foo bar"
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text_shingle"
argument_list|,
literal|"bar baz"
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
literal|"text_shingle"
argument_list|,
literal|"baz biz"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
DECL|method|cleanup
specifier|public
name|void
name|cleanup
parameter_list|()
block|{
name|indexService
operator|=
literal|null
expr_stmt|;
name|shardContext
operator|=
literal|null
expr_stmt|;
name|expectedQuery
operator|=
literal|null
expr_stmt|;
name|expectedPhraseQuery
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|testMatchPhraseQuery
specifier|public
name|void
name|testMatchPhraseQuery
parameter_list|()
throws|throws
name|IOException
block|{
name|MatchPhraseQueryBuilder
name|builder
init|=
operator|new
name|MatchPhraseQueryBuilder
argument_list|(
literal|"text_shingle_unigram"
argument_list|,
literal|"foo bar baz"
argument_list|)
decl_stmt|;
name|Query
name|query
init|=
name|builder
operator|.
name|doToQuery
argument_list|(
name|shardContext
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|expectedPhraseQueryWithUnigram
argument_list|,
name|equalTo
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|=
operator|new
name|MatchPhraseQueryBuilder
argument_list|(
literal|"text_shingle"
argument_list|,
literal|"foo bar baz biz"
argument_list|)
expr_stmt|;
name|query
operator|=
name|builder
operator|.
name|doToQuery
argument_list|(
name|shardContext
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|expectedPhraseQuery
argument_list|,
name|equalTo
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMatchQuery
specifier|public
name|void
name|testMatchQuery
parameter_list|()
throws|throws
name|IOException
block|{
name|MatchQueryBuilder
name|builder
init|=
operator|new
name|MatchQueryBuilder
argument_list|(
literal|"text_shingle_unigram"
argument_list|,
literal|"foo bar baz"
argument_list|)
decl_stmt|;
name|Query
name|query
init|=
name|builder
operator|.
name|doToQuery
argument_list|(
name|shardContext
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|expectedQueryWithUnigram
argument_list|,
name|equalTo
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|=
operator|new
name|MatchQueryBuilder
argument_list|(
literal|"text_shingle"
argument_list|,
literal|"foo bar baz biz"
argument_list|)
expr_stmt|;
name|query
operator|=
name|builder
operator|.
name|doToQuery
argument_list|(
name|shardContext
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|expectedQuery
argument_list|,
name|equalTo
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultiMatchQuery
specifier|public
name|void
name|testMultiMatchQuery
parameter_list|()
throws|throws
name|IOException
block|{
name|MultiMatchQueryBuilder
name|builder
init|=
operator|new
name|MultiMatchQueryBuilder
argument_list|(
literal|"foo bar baz"
argument_list|,
literal|"text_shingle_unigram"
argument_list|)
decl_stmt|;
name|Query
name|query
init|=
name|builder
operator|.
name|doToQuery
argument_list|(
name|shardContext
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|expectedQueryWithUnigram
argument_list|,
name|equalTo
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|type
argument_list|(
name|MatchQuery
operator|.
name|Type
operator|.
name|PHRASE
argument_list|)
expr_stmt|;
name|query
operator|=
name|builder
operator|.
name|doToQuery
argument_list|(
name|shardContext
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|expectedPhraseQueryWithUnigram
argument_list|,
name|equalTo
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|=
operator|new
name|MultiMatchQueryBuilder
argument_list|(
literal|"foo bar baz biz"
argument_list|,
literal|"text_shingle"
argument_list|)
expr_stmt|;
name|query
operator|=
name|builder
operator|.
name|doToQuery
argument_list|(
name|shardContext
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|expectedQuery
argument_list|,
name|equalTo
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|type
argument_list|(
name|MatchQuery
operator|.
name|Type
operator|.
name|PHRASE
argument_list|)
expr_stmt|;
name|query
operator|=
name|builder
operator|.
name|doToQuery
argument_list|(
name|shardContext
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|expectedPhraseQuery
argument_list|,
name|equalTo
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleQueryString
specifier|public
name|void
name|testSimpleQueryString
parameter_list|()
throws|throws
name|IOException
block|{
name|SimpleQueryStringBuilder
name|builder
init|=
operator|new
name|SimpleQueryStringBuilder
argument_list|(
literal|"foo bar baz"
argument_list|)
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"text_shingle_unigram"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|flags
argument_list|(
name|SimpleQueryStringFlag
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
name|builder
operator|.
name|doToQuery
argument_list|(
name|shardContext
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|expectedQueryWithUnigram
argument_list|,
name|equalTo
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|=
operator|new
name|SimpleQueryStringBuilder
argument_list|(
literal|"\"foo bar baz\""
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"text_shingle_unigram"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|flags
argument_list|(
name|SimpleQueryStringFlag
operator|.
name|PHRASE
argument_list|)
expr_stmt|;
name|query
operator|=
name|builder
operator|.
name|doToQuery
argument_list|(
name|shardContext
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|expectedPhraseQueryWithUnigram
argument_list|,
name|equalTo
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|=
operator|new
name|SimpleQueryStringBuilder
argument_list|(
literal|"foo bar baz biz"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"text_shingle"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|flags
argument_list|(
name|SimpleQueryStringFlag
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|query
operator|=
name|builder
operator|.
name|doToQuery
argument_list|(
name|shardContext
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|expectedQuery
argument_list|,
name|equalTo
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|=
operator|new
name|SimpleQueryStringBuilder
argument_list|(
literal|"\"foo bar baz biz\""
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"text_shingle"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|flags
argument_list|(
name|SimpleQueryStringFlag
operator|.
name|PHRASE
argument_list|)
expr_stmt|;
name|query
operator|=
name|builder
operator|.
name|doToQuery
argument_list|(
name|shardContext
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|expectedPhraseQuery
argument_list|,
name|equalTo
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testQueryString
specifier|public
name|void
name|testQueryString
parameter_list|()
throws|throws
name|IOException
block|{
name|QueryStringQueryBuilder
name|builder
init|=
operator|new
name|QueryStringQueryBuilder
argument_list|(
literal|"foo bar baz"
argument_list|)
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"text_shingle_unigram"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|splitOnWhitespace
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
name|builder
operator|.
name|doToQuery
argument_list|(
name|shardContext
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|expectedQueryWithUnigram
argument_list|,
name|equalTo
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|=
operator|new
name|QueryStringQueryBuilder
argument_list|(
literal|"\"foo bar baz\""
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"text_shingle_unigram"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|splitOnWhitespace
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|query
operator|=
name|builder
operator|.
name|doToQuery
argument_list|(
name|shardContext
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|DisjunctionMaxQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|DisjunctionMaxQuery
name|maxQuery
init|=
operator|(
name|DisjunctionMaxQuery
operator|)
name|query
decl_stmt|;
name|assertThat
argument_list|(
name|maxQuery
operator|.
name|getDisjuncts
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|expectedPhraseQueryWithUnigram
argument_list|,
name|equalTo
argument_list|(
name|maxQuery
operator|.
name|getDisjuncts
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|=
operator|new
name|QueryStringQueryBuilder
argument_list|(
literal|"foo bar baz biz"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"text_shingle"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|splitOnWhitespace
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|query
operator|=
name|builder
operator|.
name|doToQuery
argument_list|(
name|shardContext
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|expectedQuery
argument_list|,
name|equalTo
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|=
operator|new
name|QueryStringQueryBuilder
argument_list|(
literal|"\"foo bar baz biz\""
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"text_shingle"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|splitOnWhitespace
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|query
operator|=
name|builder
operator|.
name|doToQuery
argument_list|(
name|shardContext
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|DisjunctionMaxQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|maxQuery
operator|=
operator|(
name|DisjunctionMaxQuery
operator|)
name|query
expr_stmt|;
name|assertThat
argument_list|(
name|maxQuery
operator|.
name|getDisjuncts
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|expectedPhraseQuery
argument_list|,
name|equalTo
argument_list|(
name|maxQuery
operator|.
name|getDisjuncts
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
