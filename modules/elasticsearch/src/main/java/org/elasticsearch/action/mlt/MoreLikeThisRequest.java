begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.mlt
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|mlt
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
name|util
operator|.
name|UnicodeUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchGenerationException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|ActionRequest
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
name|ActionRequestValidationException
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
name|Actions
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
name|search
operator|.
name|SearchType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
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
name|Bytes
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
name|Required
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
name|Strings
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
name|Unicode
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
name|io
operator|.
name|FastByteArrayOutputStream
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentFactory
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
name|xcontent
operator|.
name|XContentType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|Scroll
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|builder
operator|.
name|SearchSourceBuilder
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
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|Scroll
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * A more like this request allowing to search for documents that a "like" the provided document. The document  * to check against to fetched based on the index, type and id provided. Best created with {@link org.elasticsearch.client.Requests#moreLikeThisRequest(String)}.  *  *<p>Note, the {@link #index()}, {@link #type(String)} and {@link #id(String)} are required.  *  * @author kimchy (shay.banon)  * @see org.elasticsearch.client.Client#moreLikeThis(MoreLikeThisRequest)  * @see org.elasticsearch.client.Requests#moreLikeThisRequest(String)  * @see org.elasticsearch.action.search.SearchResponse  */
end_comment

begin_class
DECL|class|MoreLikeThisRequest
specifier|public
class|class
name|MoreLikeThisRequest
implements|implements
name|ActionRequest
block|{
DECL|field|contentType
specifier|private
specifier|static
specifier|final
name|XContentType
name|contentType
init|=
name|Requests
operator|.
name|CONTENT_TYPE
decl_stmt|;
DECL|field|index
specifier|private
name|String
name|index
decl_stmt|;
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|field|fields
specifier|private
name|String
index|[]
name|fields
decl_stmt|;
DECL|field|percentTermsToMatch
specifier|private
name|float
name|percentTermsToMatch
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|minTermFreq
specifier|private
name|int
name|minTermFreq
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|maxQueryTerms
specifier|private
name|int
name|maxQueryTerms
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|stopWords
specifier|private
name|String
index|[]
name|stopWords
init|=
literal|null
decl_stmt|;
DECL|field|minDocFreq
specifier|private
name|int
name|minDocFreq
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|maxDocFreq
specifier|private
name|int
name|maxDocFreq
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|minWordLen
specifier|private
name|int
name|minWordLen
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|maxWordLen
specifier|private
name|int
name|maxWordLen
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|boostTerms
specifier|private
name|float
name|boostTerms
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|searchType
specifier|private
name|SearchType
name|searchType
init|=
name|SearchType
operator|.
name|DEFAULT
decl_stmt|;
DECL|field|searchQueryHint
specifier|private
name|String
name|searchQueryHint
decl_stmt|;
DECL|field|searchIndices
specifier|private
name|String
index|[]
name|searchIndices
decl_stmt|;
DECL|field|searchTypes
specifier|private
name|String
index|[]
name|searchTypes
decl_stmt|;
DECL|field|searchScroll
specifier|private
name|Scroll
name|searchScroll
decl_stmt|;
DECL|field|searchSource
specifier|private
name|byte
index|[]
name|searchSource
decl_stmt|;
DECL|field|searchSourceOffset
specifier|private
name|int
name|searchSourceOffset
decl_stmt|;
DECL|field|searchSourceLength
specifier|private
name|int
name|searchSourceLength
decl_stmt|;
DECL|field|searchSourceUnsafe
specifier|private
name|boolean
name|searchSourceUnsafe
decl_stmt|;
DECL|field|threadedListener
specifier|private
name|boolean
name|threadedListener
init|=
literal|false
decl_stmt|;
DECL|method|MoreLikeThisRequest
name|MoreLikeThisRequest
parameter_list|()
block|{     }
comment|/**      * Constructs a new more like this request for a document that will be fetch from the provided index.      * Use {@link #type(String)} and {@link #id(String)} to specificy the document to load.      */
DECL|method|MoreLikeThisRequest
specifier|public
name|MoreLikeThisRequest
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
comment|/**      * The index to load the document from which the "like" query will run with.      */
DECL|method|index
specifier|public
name|String
name|index
parameter_list|()
block|{
return|return
name|index
return|;
block|}
comment|/**      * The type of document to load from which the "like" query will rutn with.      */
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|type
return|;
block|}
DECL|method|index
name|void
name|index
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
comment|/**      * The type of document to load from which the "like" query will rutn with.      */
DECL|method|type
annotation|@
name|Required
specifier|public
name|MoreLikeThisRequest
name|type
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
return|return
name|this
return|;
block|}
comment|/**      * The id of document to load from which the "like" query will rutn with.      */
DECL|method|id
specifier|public
name|String
name|id
parameter_list|()
block|{
return|return
name|id
return|;
block|}
comment|/**      * The id of document to load from which the "like" query will rutn with.      */
DECL|method|id
annotation|@
name|Required
specifier|public
name|MoreLikeThisRequest
name|id
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
return|return
name|this
return|;
block|}
comment|/**      * The fields of the document to use in order to find documents "like" this one. Defaults to run      * against all the document fields.      */
DECL|method|fields
specifier|public
name|String
index|[]
name|fields
parameter_list|()
block|{
return|return
name|this
operator|.
name|fields
return|;
block|}
comment|/**      * The fields of the document to use in order to find documents "like" this one. Defaults to run      * against all the document fields.      */
DECL|method|fields
specifier|public
name|MoreLikeThisRequest
name|fields
parameter_list|(
name|String
modifier|...
name|fields
parameter_list|)
block|{
name|this
operator|.
name|fields
operator|=
name|fields
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The percent of the terms to match for each field. Defaults to<tt>0.3f</tt>.      */
DECL|method|percentTermsToMatch
specifier|public
name|MoreLikeThisRequest
name|percentTermsToMatch
parameter_list|(
name|float
name|percentTermsToMatch
parameter_list|)
block|{
name|this
operator|.
name|percentTermsToMatch
operator|=
name|percentTermsToMatch
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The percent of the terms to match for each field. Defaults to<tt>0.3f</tt>.      */
DECL|method|percentTermsToMatch
specifier|public
name|float
name|percentTermsToMatch
parameter_list|()
block|{
return|return
name|this
operator|.
name|percentTermsToMatch
return|;
block|}
comment|/**      * The frequency below which terms will be ignored in the source doc. Defaults to<tt>2</tt>.      */
DECL|method|minTermFreq
specifier|public
name|MoreLikeThisRequest
name|minTermFreq
parameter_list|(
name|int
name|minTermFreq
parameter_list|)
block|{
name|this
operator|.
name|minTermFreq
operator|=
name|minTermFreq
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The frequency below which terms will be ignored in the source doc. Defaults to<tt>2</tt>.      */
DECL|method|minTermFreq
specifier|public
name|int
name|minTermFreq
parameter_list|()
block|{
return|return
name|this
operator|.
name|minTermFreq
return|;
block|}
comment|/**      * The maximum number of query terms that will be included in any generated query. Defaults to<tt>25</tt>.      */
DECL|method|maxQueryTerms
specifier|public
name|MoreLikeThisRequest
name|maxQueryTerms
parameter_list|(
name|int
name|maxQueryTerms
parameter_list|)
block|{
name|this
operator|.
name|maxQueryTerms
operator|=
name|maxQueryTerms
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The maximum number of query terms that will be included in any generated query. Defaults to<tt>25</tt>.      */
DECL|method|maxQueryTerms
specifier|public
name|int
name|maxQueryTerms
parameter_list|()
block|{
return|return
name|this
operator|.
name|maxQueryTerms
return|;
block|}
comment|/**      * Any word in this set is considered "uninteresting" and ignored.      *      *<p>Even if your Analyzer allows stopwords, you might want to tell the MoreLikeThis code to ignore them, as      * for the purposes of document similarity it seems reasonable to assume that "a stop word is never interesting".      *      *<p>Defaults to no stop words.      */
DECL|method|stopWords
specifier|public
name|MoreLikeThisRequest
name|stopWords
parameter_list|(
name|String
modifier|...
name|stopWords
parameter_list|)
block|{
name|this
operator|.
name|stopWords
operator|=
name|stopWords
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Any word in this set is considered "uninteresting" and ignored.      *      *<p>Even if your Analyzer allows stopwords, you might want to tell the MoreLikeThis code to ignore them, as      * for the purposes of document similarity it seems reasonable to assume that "a stop word is never interesting".      *      *<p>Defaults to no stop words.      */
DECL|method|stopWords
specifier|public
name|String
index|[]
name|stopWords
parameter_list|()
block|{
return|return
name|this
operator|.
name|stopWords
return|;
block|}
comment|/**      * The frequency at which words will be ignored which do not occur in at least this      * many docs. Defaults to<tt>5</tt>.      */
DECL|method|minDocFreq
specifier|public
name|MoreLikeThisRequest
name|minDocFreq
parameter_list|(
name|int
name|minDocFreq
parameter_list|)
block|{
name|this
operator|.
name|minDocFreq
operator|=
name|minDocFreq
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The frequency at which words will be ignored which do not occur in at least this      * many docs. Defaults to<tt>5</tt>.      */
DECL|method|minDocFreq
specifier|public
name|int
name|minDocFreq
parameter_list|()
block|{
return|return
name|this
operator|.
name|minDocFreq
return|;
block|}
comment|/**      * The maximum frequency in which words may still appear. Words that appear      * in more than this many docs will be ignored. Defaults to unbounded.      */
DECL|method|maxDocFreq
specifier|public
name|MoreLikeThisRequest
name|maxDocFreq
parameter_list|(
name|int
name|maxDocFreq
parameter_list|)
block|{
name|this
operator|.
name|maxDocFreq
operator|=
name|maxDocFreq
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The maximum frequency in which words may still appear. Words that appear      * in more than this many docs will be ignored. Defaults to unbounded.      */
DECL|method|maxDocFreq
specifier|public
name|int
name|maxDocFreq
parameter_list|()
block|{
return|return
name|this
operator|.
name|maxDocFreq
return|;
block|}
comment|/**      * The minimum word length below which words will be ignored. Defaults to<tt>0</tt>.      */
DECL|method|minWordLen
specifier|public
name|MoreLikeThisRequest
name|minWordLen
parameter_list|(
name|int
name|minWordLen
parameter_list|)
block|{
name|this
operator|.
name|minWordLen
operator|=
name|minWordLen
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The minimum word length below which words will be ignored. Defaults to<tt>0</tt>.      */
DECL|method|minWordLen
specifier|public
name|int
name|minWordLen
parameter_list|()
block|{
return|return
name|this
operator|.
name|minWordLen
return|;
block|}
comment|/**      * The maximum word length above which words will be ignored. Defaults to unbounded.      */
DECL|method|maxWordLen
specifier|public
name|MoreLikeThisRequest
name|maxWordLen
parameter_list|(
name|int
name|maxWordLen
parameter_list|)
block|{
name|this
operator|.
name|maxWordLen
operator|=
name|maxWordLen
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The maximum word length above which words will be ignored. Defaults to unbounded.      */
DECL|method|maxWordLen
specifier|public
name|int
name|maxWordLen
parameter_list|()
block|{
return|return
name|this
operator|.
name|maxWordLen
return|;
block|}
comment|/**      * The boost factor to use when boosting terms. Defaults to<tt>1</tt>.      */
DECL|method|boostTerms
specifier|public
name|MoreLikeThisRequest
name|boostTerms
parameter_list|(
name|float
name|boostTerms
parameter_list|)
block|{
name|this
operator|.
name|boostTerms
operator|=
name|boostTerms
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The boost factor to use when boosting terms. Defaults to<tt>1</tt>.      */
DECL|method|boostTerms
specifier|public
name|float
name|boostTerms
parameter_list|()
block|{
return|return
name|this
operator|.
name|boostTerms
return|;
block|}
DECL|method|beforeLocalFork
name|void
name|beforeLocalFork
parameter_list|()
block|{
if|if
condition|(
name|searchSourceUnsafe
condition|)
block|{
name|searchSource
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|searchSource
argument_list|,
name|searchSourceOffset
argument_list|,
name|searchSourceOffset
operator|+
name|searchSourceLength
argument_list|)
expr_stmt|;
name|searchSourceOffset
operator|=
literal|0
expr_stmt|;
name|searchSourceUnsafe
operator|=
literal|false
expr_stmt|;
block|}
block|}
comment|/**      * An optional search source request allowing to control the search request for the      * more like this documents.      */
DECL|method|searchSource
specifier|public
name|MoreLikeThisRequest
name|searchSource
parameter_list|(
name|SearchSourceBuilder
name|sourceBuilder
parameter_list|)
block|{
name|FastByteArrayOutputStream
name|bos
init|=
name|sourceBuilder
operator|.
name|buildAsUnsafeBytes
argument_list|(
name|Requests
operator|.
name|CONTENT_TYPE
argument_list|)
decl_stmt|;
name|this
operator|.
name|searchSource
operator|=
name|bos
operator|.
name|unsafeByteArray
argument_list|()
expr_stmt|;
name|this
operator|.
name|searchSourceOffset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|searchSourceLength
operator|=
name|bos
operator|.
name|size
argument_list|()
expr_stmt|;
name|this
operator|.
name|searchSourceUnsafe
operator|=
literal|true
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * An optional search source request allowing to control the search request for the      * more like this documents.      */
DECL|method|searchSource
specifier|public
name|MoreLikeThisRequest
name|searchSource
parameter_list|(
name|String
name|searchSource
parameter_list|)
block|{
name|UnicodeUtil
operator|.
name|UTF8Result
name|result
init|=
name|Unicode
operator|.
name|fromStringAsUtf8
argument_list|(
name|searchSource
argument_list|)
decl_stmt|;
name|this
operator|.
name|searchSource
operator|=
name|result
operator|.
name|result
expr_stmt|;
name|this
operator|.
name|searchSourceOffset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|searchSourceLength
operator|=
name|result
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|searchSourceUnsafe
operator|=
literal|true
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|searchSource
specifier|public
name|MoreLikeThisRequest
name|searchSource
parameter_list|(
name|Map
name|searchSource
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|contentType
argument_list|)
decl_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|searchSource
argument_list|)
expr_stmt|;
return|return
name|searchSource
argument_list|(
name|builder
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchGenerationException
argument_list|(
literal|"Failed to generate ["
operator|+
name|searchSource
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|searchSource
specifier|public
name|MoreLikeThisRequest
name|searchSource
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|)
block|{
try|try
block|{
name|this
operator|.
name|searchSource
operator|=
name|builder
operator|.
name|unsafeBytes
argument_list|()
expr_stmt|;
name|this
operator|.
name|searchSourceOffset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|searchSourceLength
operator|=
name|builder
operator|.
name|unsafeBytesLength
argument_list|()
expr_stmt|;
name|this
operator|.
name|searchSourceUnsafe
operator|=
literal|true
expr_stmt|;
return|return
name|this
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchGenerationException
argument_list|(
literal|"Failed to generate ["
operator|+
name|builder
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * An optional search source request allowing to control the search request for the      * more like this documents.      */
DECL|method|searchSource
specifier|public
name|MoreLikeThisRequest
name|searchSource
parameter_list|(
name|byte
index|[]
name|searchSource
parameter_list|)
block|{
return|return
name|searchSource
argument_list|(
name|searchSource
argument_list|,
literal|0
argument_list|,
name|searchSource
operator|.
name|length
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**      * An optional search source request allowing to control the search request for the      * more like this documents.      */
DECL|method|searchSource
specifier|public
name|MoreLikeThisRequest
name|searchSource
parameter_list|(
name|byte
index|[]
name|searchSource
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|boolean
name|unsafe
parameter_list|)
block|{
name|this
operator|.
name|searchSource
operator|=
name|searchSource
expr_stmt|;
name|this
operator|.
name|searchSourceOffset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|searchSourceLength
operator|=
name|length
expr_stmt|;
name|this
operator|.
name|searchSourceUnsafe
operator|=
name|unsafe
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * An optional search source request allowing to control the search request for the      * more like this documents.      */
DECL|method|searchSource
specifier|public
name|byte
index|[]
name|searchSource
parameter_list|()
block|{
return|return
name|this
operator|.
name|searchSource
return|;
block|}
DECL|method|searchSourceOffset
specifier|public
name|int
name|searchSourceOffset
parameter_list|()
block|{
return|return
name|searchSourceOffset
return|;
block|}
DECL|method|searchSourceLength
specifier|public
name|int
name|searchSourceLength
parameter_list|()
block|{
return|return
name|searchSourceLength
return|;
block|}
DECL|method|searchSourceUnsafe
specifier|public
name|boolean
name|searchSourceUnsafe
parameter_list|()
block|{
return|return
name|searchSourceUnsafe
return|;
block|}
comment|/**      * The search type of the mlt search query.      */
DECL|method|searchType
specifier|public
name|MoreLikeThisRequest
name|searchType
parameter_list|(
name|SearchType
name|searchType
parameter_list|)
block|{
name|this
operator|.
name|searchType
operator|=
name|searchType
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The search type of the mlt search query.      */
DECL|method|searchType
specifier|public
name|MoreLikeThisRequest
name|searchType
parameter_list|(
name|String
name|searchType
parameter_list|)
throws|throws
name|ElasticSearchIllegalArgumentException
block|{
return|return
name|searchType
argument_list|(
name|SearchType
operator|.
name|fromString
argument_list|(
name|searchType
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * The search type of the mlt search query.      */
DECL|method|searchType
specifier|public
name|SearchType
name|searchType
parameter_list|()
block|{
return|return
name|this
operator|.
name|searchType
return|;
block|}
comment|/**      * The indices the resulting mlt query will run against. If not set, will run      * against the index the document was fetched from.      */
DECL|method|searchIndices
specifier|public
name|MoreLikeThisRequest
name|searchIndices
parameter_list|(
name|String
modifier|...
name|searchIndices
parameter_list|)
block|{
name|this
operator|.
name|searchIndices
operator|=
name|searchIndices
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The indices the resulting mlt query will run against. If not set, will run      * against the index the document was fetched from.      */
DECL|method|searchIndices
specifier|public
name|String
index|[]
name|searchIndices
parameter_list|()
block|{
return|return
name|this
operator|.
name|searchIndices
return|;
block|}
comment|/**      * The types the resulting mlt query will run against. If not set, will run      * against the type of the document fetched.      */
DECL|method|searchTypes
specifier|public
name|MoreLikeThisRequest
name|searchTypes
parameter_list|(
name|String
modifier|...
name|searchTypes
parameter_list|)
block|{
name|this
operator|.
name|searchTypes
operator|=
name|searchTypes
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The types the resulting mlt query will run against. If not set, will run      * against the type of the document fetched.      */
DECL|method|searchTypes
specifier|public
name|String
index|[]
name|searchTypes
parameter_list|()
block|{
return|return
name|this
operator|.
name|searchTypes
return|;
block|}
comment|/**      * Optional search query hint.      */
DECL|method|searchQueryHint
specifier|public
name|MoreLikeThisRequest
name|searchQueryHint
parameter_list|(
name|String
name|searchQueryHint
parameter_list|)
block|{
name|this
operator|.
name|searchQueryHint
operator|=
name|searchQueryHint
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Optional search query hint.      */
DECL|method|searchQueryHint
specifier|public
name|String
name|searchQueryHint
parameter_list|()
block|{
return|return
name|this
operator|.
name|searchQueryHint
return|;
block|}
comment|/**      * An optional search scroll request to be able to continue and scroll the search      * operation.      */
DECL|method|searchScroll
specifier|public
name|MoreLikeThisRequest
name|searchScroll
parameter_list|(
name|Scroll
name|searchScroll
parameter_list|)
block|{
name|this
operator|.
name|searchScroll
operator|=
name|searchScroll
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * An optional search scroll request to be able to continue and scroll the search      * operation.      */
DECL|method|searchScroll
specifier|public
name|Scroll
name|searchScroll
parameter_list|()
block|{
return|return
name|this
operator|.
name|searchScroll
return|;
block|}
DECL|method|validate
annotation|@
name|Override
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
name|ActionRequestValidationException
name|validationException
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|index
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|Actions
operator|.
name|addValidationError
argument_list|(
literal|"index is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|Actions
operator|.
name|addValidationError
argument_list|(
literal|"type is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|id
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|Actions
operator|.
name|addValidationError
argument_list|(
literal|"id is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
comment|/**      * Should the listener be called on a separate thread if needed.      */
DECL|method|listenerThreaded
annotation|@
name|Override
specifier|public
name|boolean
name|listenerThreaded
parameter_list|()
block|{
return|return
name|threadedListener
return|;
block|}
comment|/**      * Should the listener be called on a separate thread if needed.      */
DECL|method|listenerThreaded
annotation|@
name|Override
specifier|public
name|ActionRequest
name|listenerThreaded
parameter_list|(
name|boolean
name|listenerThreaded
parameter_list|)
block|{
name|this
operator|.
name|threadedListener
operator|=
name|listenerThreaded
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|index
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|type
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|id
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
comment|// no need to pass threading over the network, they are always false when coming throw a thread pool
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
name|fields
operator|=
name|Strings
operator|.
name|EMPTY_ARRAY
expr_stmt|;
block|}
else|else
block|{
name|fields
operator|=
operator|new
name|String
index|[
name|size
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|fields
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
block|}
name|percentTermsToMatch
operator|=
name|in
operator|.
name|readFloat
argument_list|()
expr_stmt|;
name|minTermFreq
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|maxQueryTerms
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|size
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|size
operator|>
literal|0
condition|)
block|{
name|stopWords
operator|=
operator|new
name|String
index|[
name|size
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|stopWords
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
block|}
name|minDocFreq
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|maxDocFreq
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|minWordLen
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|maxWordLen
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|boostTerms
operator|=
name|in
operator|.
name|readFloat
argument_list|()
expr_stmt|;
name|searchType
operator|=
name|SearchType
operator|.
name|fromId
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|searchQueryHint
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
name|size
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
name|searchIndices
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|size
operator|==
literal|1
condition|)
block|{
name|searchIndices
operator|=
name|Strings
operator|.
name|EMPTY_ARRAY
expr_stmt|;
block|}
else|else
block|{
name|searchIndices
operator|=
operator|new
name|String
index|[
name|size
operator|-
literal|1
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|searchIndices
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|searchIndices
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
block|}
name|size
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
name|searchTypes
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|size
operator|==
literal|1
condition|)
block|{
name|searchTypes
operator|=
name|Strings
operator|.
name|EMPTY_ARRAY
expr_stmt|;
block|}
else|else
block|{
name|searchTypes
operator|=
operator|new
name|String
index|[
name|size
operator|-
literal|1
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|searchTypes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|searchTypes
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|searchScroll
operator|=
name|readScroll
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|searchSourceUnsafe
operator|=
literal|false
expr_stmt|;
name|searchSourceOffset
operator|=
literal|0
expr_stmt|;
name|searchSourceLength
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|searchSourceLength
operator|==
literal|0
condition|)
block|{
name|searchSource
operator|=
name|Bytes
operator|.
name|EMPTY_ARRAY
expr_stmt|;
block|}
else|else
block|{
name|searchSource
operator|=
operator|new
name|byte
index|[
name|searchSourceLength
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|searchSource
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|id
argument_list|)
expr_stmt|;
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|fields
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
block|}
name|out
operator|.
name|writeFloat
argument_list|(
name|percentTermsToMatch
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|minTermFreq
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|maxQueryTerms
argument_list|)
expr_stmt|;
if|if
condition|(
name|stopWords
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|stopWords
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|stopWord
range|:
name|stopWords
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|stopWord
argument_list|)
expr_stmt|;
block|}
block|}
name|out
operator|.
name|writeVInt
argument_list|(
name|minDocFreq
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|maxDocFreq
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|minWordLen
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|maxWordLen
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeFloat
argument_list|(
name|boostTerms
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|searchType
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|searchQueryHint
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|searchQueryHint
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|searchIndices
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|searchIndices
operator|.
name|length
operator|+
literal|1
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|index
range|:
name|searchIndices
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|searchTypes
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|searchTypes
operator|.
name|length
operator|+
literal|1
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|type
range|:
name|searchTypes
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|searchScroll
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|searchScroll
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|searchSource
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|searchSourceLength
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|searchSource
argument_list|,
name|searchSourceOffset
argument_list|,
name|searchSourceLength
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

