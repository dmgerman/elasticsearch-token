begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.cache.id.simple
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|cache
operator|.
name|id
operator|.
name|simple
package|;
end_package

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|impl
operator|.
name|Constants
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
name|util
operator|.
name|StringHelper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|BytesWrap
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
name|collect
operator|.
name|MapBuilder
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
name|inject
operator|.
name|Inject
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
name|common
operator|.
name|trove
operator|.
name|ExtTObjectIntHasMap
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
name|concurrent
operator|.
name|ConcurrentCollections
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
name|AbstractIndexComponent
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
name|Index
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
name|cache
operator|.
name|id
operator|.
name|IdCache
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
name|cache
operator|.
name|id
operator|.
name|IdReaderCache
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
name|mapper
operator|.
name|Uid
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
name|mapper
operator|.
name|internal
operator|.
name|ParentFieldMapper
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
name|mapper
operator|.
name|internal
operator|.
name|UidFieldMapper
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
name|settings
operator|.
name|IndexSettings
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|Iterator
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMap
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|SimpleIdCache
specifier|public
class|class
name|SimpleIdCache
extends|extends
name|AbstractIndexComponent
implements|implements
name|IdCache
implements|,
name|SegmentReader
operator|.
name|CoreClosedListener
block|{
DECL|field|idReaders
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|Object
argument_list|,
name|SimpleIdReaderCache
argument_list|>
name|idReaders
decl_stmt|;
annotation|@
name|Inject
DECL|method|SimpleIdCache
specifier|public
name|SimpleIdCache
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|idReaders
operator|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|idReaders
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onClose
specifier|public
name|void
name|onClose
parameter_list|(
name|SegmentReader
name|owner
parameter_list|)
block|{
name|clear
argument_list|(
name|owner
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
block|{
name|idReaders
operator|.
name|remove
argument_list|(
name|reader
operator|.
name|getCoreCacheKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|reader
specifier|public
name|IdReaderCache
name|reader
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
block|{
return|return
name|idReaders
operator|.
name|get
argument_list|(
name|reader
operator|.
name|getCoreCacheKey
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|IdReaderCache
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|(
name|Iterator
argument_list|<
name|IdReaderCache
argument_list|>
operator|)
name|idReaders
operator|.
name|values
argument_list|()
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"StringEquality"
block|}
argument_list|)
annotation|@
name|Override
DECL|method|refresh
specifier|public
name|void
name|refresh
parameter_list|(
name|IndexReader
index|[]
name|readers
parameter_list|)
throws|throws
name|Exception
block|{
comment|// do a quick check for the common case, that all are there
if|if
condition|(
name|refreshNeeded
argument_list|(
name|readers
argument_list|)
condition|)
block|{
synchronized|synchronized
init|(
name|idReaders
init|)
block|{
if|if
condition|(
operator|!
name|refreshNeeded
argument_list|(
name|readers
argument_list|)
condition|)
block|{
return|return;
block|}
comment|// do the refresh
name|Map
argument_list|<
name|Object
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|TypeBuilder
argument_list|>
argument_list|>
name|builders
init|=
operator|new
name|HashMap
argument_list|<
name|Object
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|TypeBuilder
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// first, go over and load all the id->doc map for all types
for|for
control|(
name|IndexReader
name|reader
range|:
name|readers
control|)
block|{
if|if
condition|(
name|idReaders
operator|.
name|containsKey
argument_list|(
name|reader
operator|.
name|getCoreCacheKey
argument_list|()
argument_list|)
condition|)
block|{
comment|// no need, continue
continue|continue;
block|}
operator|(
operator|(
name|SegmentReader
operator|)
name|reader
operator|)
operator|.
name|addCoreClosedListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|TypeBuilder
argument_list|>
name|readerBuilder
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|TypeBuilder
argument_list|>
argument_list|()
decl_stmt|;
name|builders
operator|.
name|put
argument_list|(
name|reader
operator|.
name|getCoreCacheKey
argument_list|()
argument_list|,
name|readerBuilder
argument_list|)
expr_stmt|;
name|String
name|field
init|=
name|StringHelper
operator|.
name|intern
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
name|TermDocs
name|termDocs
init|=
name|reader
operator|.
name|termDocs
argument_list|()
decl_stmt|;
name|TermEnum
name|termEnum
init|=
name|reader
operator|.
name|terms
argument_list|(
operator|new
name|Term
argument_list|(
name|field
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
do|do
block|{
name|Term
name|term
init|=
name|termEnum
operator|.
name|term
argument_list|()
decl_stmt|;
if|if
condition|(
name|term
operator|==
literal|null
operator|||
name|term
operator|.
name|field
argument_list|()
operator|!=
name|field
condition|)
break|break;
comment|// TODO we can optimize this, since type is the prefix, and we get terms ordered
comment|// so, only need to move to the next type once its different
name|Uid
name|uid
init|=
name|Uid
operator|.
name|createUid
argument_list|(
name|term
operator|.
name|text
argument_list|()
argument_list|)
decl_stmt|;
name|TypeBuilder
name|typeBuilder
init|=
name|readerBuilder
operator|.
name|get
argument_list|(
name|uid
operator|.
name|type
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|typeBuilder
operator|==
literal|null
condition|)
block|{
name|typeBuilder
operator|=
operator|new
name|TypeBuilder
argument_list|(
name|reader
argument_list|)
expr_stmt|;
name|readerBuilder
operator|.
name|put
argument_list|(
name|StringHelper
operator|.
name|intern
argument_list|(
name|uid
operator|.
name|type
argument_list|()
argument_list|)
argument_list|,
name|typeBuilder
argument_list|)
expr_stmt|;
block|}
name|BytesWrap
name|idAsBytes
init|=
name|checkIfCanReuse
argument_list|(
name|builders
argument_list|,
operator|new
name|BytesWrap
argument_list|(
name|uid
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|termDocs
operator|.
name|seek
argument_list|(
name|termEnum
argument_list|)
expr_stmt|;
while|while
condition|(
name|termDocs
operator|.
name|next
argument_list|()
condition|)
block|{
comment|// when traversing, make sure to ignore deleted docs, so the key->docId will be correct
if|if
condition|(
operator|!
name|reader
operator|.
name|isDeleted
argument_list|(
name|termDocs
operator|.
name|doc
argument_list|()
argument_list|)
condition|)
block|{
name|typeBuilder
operator|.
name|idToDoc
operator|.
name|put
argument_list|(
name|idAsBytes
argument_list|,
name|termDocs
operator|.
name|doc
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
do|while
condition|(
name|termEnum
operator|.
name|next
argument_list|()
condition|)
do|;
block|}
finally|finally
block|{
name|termDocs
operator|.
name|close
argument_list|()
expr_stmt|;
name|termEnum
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|// now, go and load the docId->parentId map
for|for
control|(
name|IndexReader
name|reader
range|:
name|readers
control|)
block|{
if|if
condition|(
name|idReaders
operator|.
name|containsKey
argument_list|(
name|reader
operator|.
name|getCoreCacheKey
argument_list|()
argument_list|)
condition|)
block|{
comment|// no need, continue
continue|continue;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|TypeBuilder
argument_list|>
name|readerBuilder
init|=
name|builders
operator|.
name|get
argument_list|(
name|reader
operator|.
name|getCoreCacheKey
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|field
init|=
name|StringHelper
operator|.
name|intern
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
name|TermDocs
name|termDocs
init|=
name|reader
operator|.
name|termDocs
argument_list|()
decl_stmt|;
name|TermEnum
name|termEnum
init|=
name|reader
operator|.
name|terms
argument_list|(
operator|new
name|Term
argument_list|(
name|field
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
do|do
block|{
name|Term
name|term
init|=
name|termEnum
operator|.
name|term
argument_list|()
decl_stmt|;
if|if
condition|(
name|term
operator|==
literal|null
operator|||
name|term
operator|.
name|field
argument_list|()
operator|!=
name|field
condition|)
break|break;
comment|// TODO we can optimize this, since type is the prefix, and we get terms ordered
comment|// so, only need to move to the next type once its different
name|Uid
name|uid
init|=
name|Uid
operator|.
name|createUid
argument_list|(
name|term
operator|.
name|text
argument_list|()
argument_list|)
decl_stmt|;
name|TypeBuilder
name|typeBuilder
init|=
name|readerBuilder
operator|.
name|get
argument_list|(
name|uid
operator|.
name|type
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|typeBuilder
operator|==
literal|null
condition|)
block|{
name|typeBuilder
operator|=
operator|new
name|TypeBuilder
argument_list|(
name|reader
argument_list|)
expr_stmt|;
name|readerBuilder
operator|.
name|put
argument_list|(
name|StringHelper
operator|.
name|intern
argument_list|(
name|uid
operator|.
name|type
argument_list|()
argument_list|)
argument_list|,
name|typeBuilder
argument_list|)
expr_stmt|;
block|}
name|BytesWrap
name|idAsBytes
init|=
name|checkIfCanReuse
argument_list|(
name|builders
argument_list|,
operator|new
name|BytesWrap
argument_list|(
name|uid
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|added
init|=
literal|false
decl_stmt|;
comment|// optimize for when all the docs are deleted for this id
name|termDocs
operator|.
name|seek
argument_list|(
name|termEnum
argument_list|)
expr_stmt|;
while|while
condition|(
name|termDocs
operator|.
name|next
argument_list|()
condition|)
block|{
comment|// ignore deleted docs while we are at it
if|if
condition|(
operator|!
name|reader
operator|.
name|isDeleted
argument_list|(
name|termDocs
operator|.
name|doc
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|added
condition|)
block|{
name|typeBuilder
operator|.
name|parentIdsValues
operator|.
name|add
argument_list|(
name|idAsBytes
argument_list|)
expr_stmt|;
name|added
operator|=
literal|true
expr_stmt|;
block|}
name|typeBuilder
operator|.
name|parentIdsOrdinals
index|[
name|termDocs
operator|.
name|doc
argument_list|()
index|]
operator|=
name|typeBuilder
operator|.
name|t
expr_stmt|;
block|}
block|}
if|if
condition|(
name|added
condition|)
block|{
name|typeBuilder
operator|.
name|t
operator|++
expr_stmt|;
block|}
block|}
do|while
condition|(
name|termEnum
operator|.
name|next
argument_list|()
condition|)
do|;
block|}
finally|finally
block|{
name|termDocs
operator|.
name|close
argument_list|()
expr_stmt|;
name|termEnum
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|// now, build it back
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Object
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|TypeBuilder
argument_list|>
argument_list|>
name|entry
range|:
name|builders
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|MapBuilder
argument_list|<
name|String
argument_list|,
name|SimpleIdReaderTypeCache
argument_list|>
name|types
init|=
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|TypeBuilder
argument_list|>
name|typeBuilderEntry
range|:
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|types
operator|.
name|put
argument_list|(
name|typeBuilderEntry
operator|.
name|getKey
argument_list|()
argument_list|,
operator|new
name|SimpleIdReaderTypeCache
argument_list|(
name|typeBuilderEntry
operator|.
name|getKey
argument_list|()
argument_list|,
name|typeBuilderEntry
operator|.
name|getValue
argument_list|()
operator|.
name|idToDoc
argument_list|,
name|typeBuilderEntry
operator|.
name|getValue
argument_list|()
operator|.
name|parentIdsValues
operator|.
name|toArray
argument_list|(
operator|new
name|BytesWrap
index|[
name|typeBuilderEntry
operator|.
name|getValue
argument_list|()
operator|.
name|parentIdsValues
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|typeBuilderEntry
operator|.
name|getValue
argument_list|()
operator|.
name|parentIdsOrdinals
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|SimpleIdReaderCache
name|readerCache
init|=
operator|new
name|SimpleIdReaderCache
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|types
operator|.
name|immutableMap
argument_list|()
argument_list|)
decl_stmt|;
name|idReaders
operator|.
name|put
argument_list|(
name|readerCache
operator|.
name|readerCacheKey
argument_list|()
argument_list|,
name|readerCache
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|checkIfCanReuse
specifier|private
name|BytesWrap
name|checkIfCanReuse
parameter_list|(
name|Map
argument_list|<
name|Object
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|TypeBuilder
argument_list|>
argument_list|>
name|builders
parameter_list|,
name|BytesWrap
name|idAsBytes
parameter_list|)
block|{
name|BytesWrap
name|finalIdAsBytes
decl_stmt|;
comment|// go over and see if we can reuse this id
for|for
control|(
name|SimpleIdReaderCache
name|idReaderCache
range|:
name|idReaders
operator|.
name|values
argument_list|()
control|)
block|{
name|finalIdAsBytes
operator|=
name|idReaderCache
operator|.
name|canReuse
argument_list|(
name|idAsBytes
argument_list|)
expr_stmt|;
if|if
condition|(
name|finalIdAsBytes
operator|!=
literal|null
condition|)
block|{
return|return
name|finalIdAsBytes
return|;
block|}
block|}
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|TypeBuilder
argument_list|>
name|map
range|:
name|builders
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|TypeBuilder
name|typeBuilder
range|:
name|map
operator|.
name|values
argument_list|()
control|)
block|{
name|finalIdAsBytes
operator|=
name|typeBuilder
operator|.
name|canReuse
argument_list|(
name|idAsBytes
argument_list|)
expr_stmt|;
if|if
condition|(
name|finalIdAsBytes
operator|!=
literal|null
condition|)
block|{
return|return
name|finalIdAsBytes
return|;
block|}
block|}
block|}
return|return
name|idAsBytes
return|;
block|}
DECL|method|refreshNeeded
specifier|private
name|boolean
name|refreshNeeded
parameter_list|(
name|IndexReader
index|[]
name|readers
parameter_list|)
block|{
for|for
control|(
name|IndexReader
name|reader
range|:
name|readers
control|)
block|{
if|if
condition|(
operator|!
name|idReaders
operator|.
name|containsKey
argument_list|(
name|reader
operator|.
name|getCoreCacheKey
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
DECL|class|TypeBuilder
specifier|static
class|class
name|TypeBuilder
block|{
DECL|field|idToDoc
specifier|final
name|ExtTObjectIntHasMap
argument_list|<
name|BytesWrap
argument_list|>
name|idToDoc
init|=
operator|new
name|ExtTObjectIntHasMap
argument_list|<
name|BytesWrap
argument_list|>
argument_list|(
name|Constants
operator|.
name|DEFAULT_CAPACITY
argument_list|,
name|Constants
operator|.
name|DEFAULT_LOAD_FACTOR
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
DECL|field|parentIdsValues
specifier|final
name|ArrayList
argument_list|<
name|BytesWrap
argument_list|>
name|parentIdsValues
init|=
operator|new
name|ArrayList
argument_list|<
name|BytesWrap
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|parentIdsOrdinals
specifier|final
name|int
index|[]
name|parentIdsOrdinals
decl_stmt|;
DECL|field|t
name|int
name|t
init|=
literal|1
decl_stmt|;
comment|// current term number (0 indicated null value)
DECL|method|TypeBuilder
name|TypeBuilder
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
block|{
name|parentIdsOrdinals
operator|=
operator|new
name|int
index|[
name|reader
operator|.
name|maxDoc
argument_list|()
index|]
expr_stmt|;
comment|// the first one indicates null value
name|parentIdsValues
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**          * Returns an already stored instance if exists, if not, returns null;          */
DECL|method|canReuse
specifier|public
name|BytesWrap
name|canReuse
parameter_list|(
name|BytesWrap
name|id
parameter_list|)
block|{
return|return
name|idToDoc
operator|.
name|key
argument_list|(
name|id
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

