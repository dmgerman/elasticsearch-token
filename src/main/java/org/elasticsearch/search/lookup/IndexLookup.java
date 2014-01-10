begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.lookup
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|lookup
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
name|ImmutableMap
operator|.
name|Builder
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
name|index
operator|.
name|*
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
name|IndexSearcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|MinimalMap
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
name|HashMap
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

begin_class
DECL|class|IndexLookup
specifier|public
class|class
name|IndexLookup
extends|extends
name|MinimalMap
argument_list|<
name|String
argument_list|,
name|IndexField
argument_list|>
block|{
comment|/**      * Flag to pass to {@link IndexField#get(String, flags)} if you require      * offsets in the returned {@link IndexFieldTerm}.      */
DECL|field|FLAG_OFFSETS
specifier|public
specifier|static
specifier|final
name|int
name|FLAG_OFFSETS
init|=
literal|2
decl_stmt|;
comment|/**      * Flag to pass to {@link IndexField#get(String, flags)} if you require      * payloads in the returned {@link IndexFieldTerm}.      */
DECL|field|FLAG_PAYLOADS
specifier|public
specifier|static
specifier|final
name|int
name|FLAG_PAYLOADS
init|=
literal|4
decl_stmt|;
comment|/**      * Flag to pass to {@link IndexField#get(String, flags)} if you require      * frequencies in the returned {@link IndexFieldTerm}. Frequencies might be      * returned anyway for some lucene codecs even if this flag is no set.      */
DECL|field|FLAG_FREQUENCIES
specifier|public
specifier|static
specifier|final
name|int
name|FLAG_FREQUENCIES
init|=
literal|8
decl_stmt|;
comment|/**      * Flag to pass to {@link IndexField#get(String, flags)} if you require      * positions in the returned {@link IndexFieldTerm}.      */
DECL|field|FLAG_POSITIONS
specifier|public
specifier|static
specifier|final
name|int
name|FLAG_POSITIONS
init|=
literal|16
decl_stmt|;
comment|/**      * Flag to pass to {@link IndexField#get(String, flags)} if you require      * positions in the returned {@link IndexFieldTerm}.      */
DECL|field|FLAG_CACHE
specifier|public
specifier|static
specifier|final
name|int
name|FLAG_CACHE
init|=
literal|32
decl_stmt|;
comment|// Current reader from which we can get the term vectors. No info on term
comment|// and field statistics.
DECL|field|reader
specifier|private
name|AtomicReader
name|reader
decl_stmt|;
comment|// The parent reader from which we can get proper field and term
comment|// statistics
DECL|field|parentReader
specifier|private
name|CompositeReader
name|parentReader
decl_stmt|;
comment|// we need this later to get the field and term statistics of the shard
DECL|field|indexSearcher
specifier|private
name|IndexSearcher
name|indexSearcher
decl_stmt|;
comment|// we need this later to get the term statistics of the shard
DECL|field|indexReaderContext
specifier|private
name|IndexReaderContext
name|indexReaderContext
decl_stmt|;
comment|// current docId
DECL|field|docId
specifier|private
name|int
name|docId
init|=
operator|-
literal|1
decl_stmt|;
comment|// stores the objects that are used in the script. we maintain this map
comment|// because we do not want to re-initialize the objects each time a field is
comment|// accessed
DECL|field|indexFields
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|IndexField
argument_list|>
name|indexFields
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|IndexField
argument_list|>
argument_list|()
decl_stmt|;
comment|// number of documents per shard. cached here because the computation is
comment|// expensive
DECL|field|numDocs
specifier|private
name|int
name|numDocs
init|=
operator|-
literal|1
decl_stmt|;
comment|// the maximum doc number of the shard.
DECL|field|maxDoc
specifier|private
name|int
name|maxDoc
init|=
operator|-
literal|1
decl_stmt|;
comment|// number of deleted documents per shard. cached here because the
comment|// computation is expensive
DECL|field|numDeletedDocs
specifier|private
name|int
name|numDeletedDocs
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|numDocs
specifier|public
name|int
name|numDocs
parameter_list|()
block|{
if|if
condition|(
name|numDocs
operator|==
operator|-
literal|1
condition|)
block|{
name|numDocs
operator|=
name|parentReader
operator|.
name|numDocs
argument_list|()
expr_stmt|;
block|}
return|return
name|numDocs
return|;
block|}
DECL|method|maxDoc
specifier|public
name|int
name|maxDoc
parameter_list|()
block|{
if|if
condition|(
name|maxDoc
operator|==
operator|-
literal|1
condition|)
block|{
name|maxDoc
operator|=
name|parentReader
operator|.
name|maxDoc
argument_list|()
expr_stmt|;
block|}
return|return
name|maxDoc
return|;
block|}
DECL|method|numDeletedDocs
specifier|public
name|int
name|numDeletedDocs
parameter_list|()
block|{
if|if
condition|(
name|numDeletedDocs
operator|==
operator|-
literal|1
condition|)
block|{
name|numDeletedDocs
operator|=
name|parentReader
operator|.
name|numDeletedDocs
argument_list|()
expr_stmt|;
block|}
return|return
name|numDeletedDocs
return|;
block|}
DECL|method|IndexLookup
specifier|public
name|IndexLookup
parameter_list|(
name|Builder
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|builder
parameter_list|)
block|{
name|builder
operator|.
name|put
argument_list|(
literal|"_FREQUENCIES"
argument_list|,
name|IndexLookup
operator|.
name|FLAG_FREQUENCIES
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"_POSITIONS"
argument_list|,
name|IndexLookup
operator|.
name|FLAG_POSITIONS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"_OFFSETS"
argument_list|,
name|IndexLookup
operator|.
name|FLAG_OFFSETS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"_PAYLOADS"
argument_list|,
name|IndexLookup
operator|.
name|FLAG_PAYLOADS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"_CACHE"
argument_list|,
name|IndexLookup
operator|.
name|FLAG_CACHE
argument_list|)
expr_stmt|;
block|}
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|reader
operator|==
name|context
operator|.
name|reader
argument_list|()
condition|)
block|{
comment|// if we are called with the same
comment|// reader, nothing to do
return|return;
block|}
comment|// check if we have to invalidate all field and shard stats - only if
comment|// parent reader changed
if|if
condition|(
name|context
operator|.
name|parent
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|parentReader
operator|==
literal|null
condition|)
block|{
name|parentReader
operator|=
name|context
operator|.
name|parent
operator|.
name|reader
argument_list|()
expr_stmt|;
name|indexSearcher
operator|=
operator|new
name|IndexSearcher
argument_list|(
name|parentReader
argument_list|)
expr_stmt|;
name|indexReaderContext
operator|=
name|context
operator|.
name|parent
expr_stmt|;
block|}
else|else
block|{
comment|// parent reader may only be set once. TODO we could also call
comment|// indexFields.clear() here instead of assertion just to be on
comment|// the save side
assert|assert
operator|(
name|parentReader
operator|==
name|context
operator|.
name|parent
operator|.
name|reader
argument_list|()
operator|)
assert|;
block|}
block|}
else|else
block|{
assert|assert
name|parentReader
operator|==
literal|null
assert|;
block|}
name|reader
operator|=
name|context
operator|.
name|reader
argument_list|()
expr_stmt|;
name|docId
operator|=
operator|-
literal|1
expr_stmt|;
name|setReaderInFields
argument_list|()
expr_stmt|;
block|}
DECL|method|setReaderInFields
specifier|protected
name|void
name|setReaderInFields
parameter_list|()
block|{
for|for
control|(
name|IndexField
name|stat
range|:
name|indexFields
operator|.
name|values
argument_list|()
control|)
block|{
name|stat
operator|.
name|setReader
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|setNextDocId
specifier|public
name|void
name|setNextDocId
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|docId
operator|==
name|docId
condition|)
block|{
comment|// if we are called with the same docId,
comment|// nothing to do
return|return;
block|}
comment|// We assume that docs are processed in ascending order of id. If this
comment|// is not the case, we would have to re initialize all posting lists in
comment|// IndexFieldTerm. TODO: Instead of assert we could also call
comment|// setReaderInFields(); here?
if|if
condition|(
name|this
operator|.
name|docId
operator|>
name|docId
condition|)
block|{
comment|// This might happen if the same SearchLookup is used in different
comment|// phases, such as score and fetch phase.
comment|// In this case we do not want to re initialize posting list etc.
comment|// because we do not even know if term and field statistics will be
comment|// needed in this new phase.
comment|// Therefore we just remove all IndexFieldTerms.
name|indexFields
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
name|setNextDocIdInFields
argument_list|()
expr_stmt|;
block|}
DECL|method|setNextDocIdInFields
specifier|protected
name|void
name|setNextDocIdInFields
parameter_list|()
block|{
for|for
control|(
name|IndexField
name|stat
range|:
name|indexFields
operator|.
name|values
argument_list|()
control|)
block|{
name|stat
operator|.
name|setDocIdInTerms
argument_list|(
name|this
operator|.
name|docId
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*      * TODO: here might be potential for running time improvement? If we knew in      * advance which terms are requested, we could provide an array which the      * user could then iterate over.      */
annotation|@
name|Override
DECL|method|get
specifier|public
name|IndexField
name|get
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
name|String
name|stringField
init|=
operator|(
name|String
operator|)
name|key
decl_stmt|;
name|IndexField
name|indexField
init|=
name|indexFields
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexField
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|indexField
operator|=
operator|new
name|IndexField
argument_list|(
name|stringField
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|indexFields
operator|.
name|put
argument_list|(
name|stringField
argument_list|,
name|indexField
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
return|return
name|indexField
return|;
block|}
comment|/*      * Get the lucene term vectors. See      * https://lucene.apache.org/core/4_0_0/core/org/apache/lucene/index/Fields.html      * *      */
DECL|method|termVectors
specifier|public
name|Fields
name|termVectors
parameter_list|()
throws|throws
name|IOException
block|{
assert|assert
name|reader
operator|!=
literal|null
assert|;
return|return
name|reader
operator|.
name|getTermVectors
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getReader
name|AtomicReader
name|getReader
parameter_list|()
block|{
return|return
name|reader
return|;
block|}
DECL|method|getDocId
specifier|public
name|int
name|getDocId
parameter_list|()
block|{
return|return
name|docId
return|;
block|}
DECL|method|getParentReader
specifier|public
name|IndexReader
name|getParentReader
parameter_list|()
block|{
if|if
condition|(
name|parentReader
operator|==
literal|null
condition|)
block|{
return|return
name|reader
return|;
block|}
return|return
name|parentReader
return|;
block|}
DECL|method|getIndexSearcher
specifier|public
name|IndexSearcher
name|getIndexSearcher
parameter_list|()
block|{
if|if
condition|(
name|indexSearcher
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|IndexSearcher
argument_list|(
name|reader
argument_list|)
return|;
block|}
return|return
name|indexSearcher
return|;
block|}
DECL|method|getReaderContext
specifier|public
name|IndexReaderContext
name|getReaderContext
parameter_list|()
block|{
return|return
name|indexReaderContext
return|;
block|}
block|}
end_class

end_unit

