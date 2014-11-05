begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata.plain
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|plain
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|IntArrayList
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
name|Bits
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
name|BytesRef
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|List
import|;
end_import

begin_comment
comment|/**  * Intersects the terms and unions the doc ids for terms enum of multiple fields.  *  * @elasticsearch.internal  */
end_comment

begin_class
DECL|class|ParentChildIntersectTermsEnum
specifier|final
class|class
name|ParentChildIntersectTermsEnum
extends|extends
name|TermsEnum
block|{
DECL|field|states
specifier|private
specifier|final
name|List
argument_list|<
name|TermsEnumState
argument_list|>
name|states
decl_stmt|;
DECL|field|stateSlots
specifier|private
specifier|final
name|IntArrayList
name|stateSlots
decl_stmt|;
DECL|field|current
specifier|private
name|BytesRef
name|current
decl_stmt|;
DECL|method|ParentChildIntersectTermsEnum
name|ParentChildIntersectTermsEnum
parameter_list|(
name|LeafReader
name|atomicReader
parameter_list|,
name|String
modifier|...
name|fields
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|TermsEnum
argument_list|>
name|fieldEnums
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|Terms
name|terms
init|=
name|atomicReader
operator|.
name|terms
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|terms
operator|!=
literal|null
condition|)
block|{
name|fieldEnums
operator|.
name|add
argument_list|(
name|terms
operator|.
name|iterator
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|states
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|fieldEnums
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|TermsEnum
name|tEnum
range|:
name|fieldEnums
control|)
block|{
name|states
operator|.
name|add
argument_list|(
operator|new
name|TermsEnumState
argument_list|(
name|tEnum
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|stateSlots
operator|=
operator|new
name|IntArrayList
argument_list|(
name|states
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|term
specifier|public
name|BytesRef
name|term
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|current
return|;
block|}
annotation|@
name|Override
DECL|method|docs
specifier|public
name|DocsEnum
name|docs
parameter_list|(
name|Bits
name|liveDocs
parameter_list|,
name|DocsEnum
name|reuse
parameter_list|,
name|int
name|flags
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|size
init|=
name|stateSlots
operator|.
name|size
argument_list|()
decl_stmt|;
assert|assert
name|size
operator|>
literal|0
assert|;
if|if
condition|(
name|size
operator|==
literal|1
condition|)
block|{
comment|// Can't use 'reuse' since we don't know to which previous TermsEnum it belonged to.
return|return
name|states
operator|.
name|get
argument_list|(
name|stateSlots
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|termsEnum
operator|.
name|docs
argument_list|(
name|liveDocs
argument_list|,
literal|null
argument_list|,
name|flags
argument_list|)
return|;
block|}
else|else
block|{
name|List
argument_list|<
name|DocsEnum
argument_list|>
name|docsEnums
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|stateSlots
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|stateSlots
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|docsEnums
operator|.
name|add
argument_list|(
name|states
operator|.
name|get
argument_list|(
name|stateSlots
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|termsEnum
operator|.
name|docs
argument_list|(
name|liveDocs
argument_list|,
literal|null
argument_list|,
name|flags
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|CompoundDocsEnum
argument_list|(
name|docsEnums
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|next
specifier|public
name|BytesRef
name|next
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|states
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|current
operator|==
literal|null
condition|)
block|{
comment|// unpositioned
for|for
control|(
name|TermsEnumState
name|state
range|:
name|states
control|)
block|{
name|state
operator|.
name|initialize
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|int
name|removed
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|stateSlots
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|int
name|stateSlot
init|=
name|stateSlots
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|states
operator|.
name|get
argument_list|(
name|stateSlot
operator|-
name|removed
argument_list|)
operator|.
name|next
argument_list|()
operator|==
literal|null
condition|)
block|{
name|states
operator|.
name|remove
argument_list|(
name|stateSlot
operator|-
name|removed
argument_list|)
expr_stmt|;
name|removed
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
name|states
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|stateSlots
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|BytesRef
name|lowestTerm
init|=
name|states
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|term
decl_stmt|;
name|stateSlots
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|states
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|TermsEnumState
name|state
init|=
name|states
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|int
name|cmp
init|=
name|lowestTerm
operator|.
name|compareTo
argument_list|(
name|state
operator|.
name|term
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|>
literal|0
condition|)
block|{
name|lowestTerm
operator|=
name|state
operator|.
name|term
expr_stmt|;
name|stateSlots
operator|.
name|clear
argument_list|()
expr_stmt|;
name|stateSlots
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmp
operator|==
literal|0
condition|)
block|{
name|stateSlots
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|current
operator|=
name|lowestTerm
return|;
block|}
annotation|@
name|Override
DECL|method|seekCeil
specifier|public
name|SeekStatus
name|seekCeil
parameter_list|(
name|BytesRef
name|text
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|states
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|SeekStatus
operator|.
name|END
return|;
block|}
name|boolean
name|found
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|current
operator|==
literal|null
condition|)
block|{
comment|// unpositioned
name|Iterator
argument_list|<
name|TermsEnumState
argument_list|>
name|iterator
init|=
name|states
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|SeekStatus
name|seekStatus
init|=
name|iterator
operator|.
name|next
argument_list|()
operator|.
name|seekCeil
argument_list|(
name|text
argument_list|)
decl_stmt|;
if|if
condition|(
name|seekStatus
operator|==
name|SeekStatus
operator|.
name|END
condition|)
block|{
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|seekStatus
operator|==
name|SeekStatus
operator|.
name|FOUND
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|int
name|removed
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|stateSlots
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|int
name|stateSlot
init|=
name|stateSlots
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|SeekStatus
name|seekStatus
init|=
name|states
operator|.
name|get
argument_list|(
name|stateSlot
operator|-
name|removed
argument_list|)
operator|.
name|seekCeil
argument_list|(
name|text
argument_list|)
decl_stmt|;
if|if
condition|(
name|seekStatus
operator|==
name|SeekStatus
operator|.
name|END
condition|)
block|{
name|states
operator|.
name|remove
argument_list|(
name|stateSlot
operator|-
name|removed
argument_list|)
expr_stmt|;
name|removed
operator|++
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|seekStatus
operator|==
name|SeekStatus
operator|.
name|FOUND
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|states
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|SeekStatus
operator|.
name|END
return|;
block|}
name|stateSlots
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|found
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
name|states
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|states
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|term
operator|.
name|equals
argument_list|(
name|text
argument_list|)
condition|)
block|{
name|stateSlots
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
name|current
operator|=
name|text
expr_stmt|;
return|return
name|SeekStatus
operator|.
name|FOUND
return|;
block|}
else|else
block|{
name|BytesRef
name|lowestTerm
init|=
name|states
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|term
decl_stmt|;
name|stateSlots
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|states
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|TermsEnumState
name|state
init|=
name|states
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|int
name|cmp
init|=
name|lowestTerm
operator|.
name|compareTo
argument_list|(
name|state
operator|.
name|term
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|>
literal|0
condition|)
block|{
name|lowestTerm
operator|=
name|state
operator|.
name|term
expr_stmt|;
name|stateSlots
operator|.
name|clear
argument_list|()
expr_stmt|;
name|stateSlots
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmp
operator|==
literal|0
condition|)
block|{
name|stateSlots
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
name|current
operator|=
name|lowestTerm
expr_stmt|;
return|return
name|SeekStatus
operator|.
name|NOT_FOUND
return|;
block|}
block|}
DECL|class|TermsEnumState
class|class
name|TermsEnumState
block|{
DECL|field|termsEnum
specifier|final
name|TermsEnum
name|termsEnum
decl_stmt|;
DECL|field|term
name|BytesRef
name|term
decl_stmt|;
DECL|field|lastSeekStatus
name|SeekStatus
name|lastSeekStatus
decl_stmt|;
DECL|method|TermsEnumState
name|TermsEnumState
parameter_list|(
name|TermsEnum
name|termsEnum
parameter_list|)
block|{
name|this
operator|.
name|termsEnum
operator|=
name|termsEnum
expr_stmt|;
block|}
DECL|method|initialize
name|void
name|initialize
parameter_list|()
throws|throws
name|IOException
block|{
name|term
operator|=
name|termsEnum
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
DECL|method|next
name|BytesRef
name|next
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|term
operator|=
name|termsEnum
operator|.
name|next
argument_list|()
return|;
block|}
DECL|method|seekCeil
name|SeekStatus
name|seekCeil
parameter_list|(
name|BytesRef
name|text
parameter_list|)
throws|throws
name|IOException
block|{
name|lastSeekStatus
operator|=
name|termsEnum
operator|.
name|seekCeil
argument_list|(
name|text
argument_list|)
expr_stmt|;
if|if
condition|(
name|lastSeekStatus
operator|!=
name|SeekStatus
operator|.
name|END
condition|)
block|{
name|term
operator|=
name|termsEnum
operator|.
name|term
argument_list|()
expr_stmt|;
block|}
return|return
name|lastSeekStatus
return|;
block|}
block|}
DECL|class|CompoundDocsEnum
class|class
name|CompoundDocsEnum
extends|extends
name|DocsEnum
block|{
DECL|field|states
specifier|final
name|List
argument_list|<
name|State
argument_list|>
name|states
decl_stmt|;
DECL|field|current
name|int
name|current
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|CompoundDocsEnum
name|CompoundDocsEnum
parameter_list|(
name|List
argument_list|<
name|DocsEnum
argument_list|>
name|docsEnums
parameter_list|)
block|{
name|this
operator|.
name|states
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|docsEnums
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|DocsEnum
name|docsEnum
range|:
name|docsEnums
control|)
block|{
name|states
operator|.
name|add
argument_list|(
operator|new
name|State
argument_list|(
name|docsEnum
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|freq
specifier|public
name|int
name|freq
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|docID
specifier|public
name|int
name|docID
parameter_list|()
block|{
return|return
name|current
return|;
block|}
annotation|@
name|Override
DECL|method|nextDoc
specifier|public
name|int
name|nextDoc
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|states
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|current
operator|=
name|NO_MORE_DOCS
return|;
block|}
if|if
condition|(
name|current
operator|==
operator|-
literal|1
condition|)
block|{
for|for
control|(
name|State
name|state
range|:
name|states
control|)
block|{
name|state
operator|.
name|initialize
argument_list|()
expr_stmt|;
block|}
block|}
name|int
name|lowestIndex
init|=
literal|0
decl_stmt|;
name|int
name|lowestDocId
init|=
name|states
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|current
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|states
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|State
name|state
init|=
name|states
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|lowestDocId
operator|>
name|state
operator|.
name|current
condition|)
block|{
name|lowestDocId
operator|=
name|state
operator|.
name|current
expr_stmt|;
name|lowestIndex
operator|=
name|i
expr_stmt|;
block|}
block|}
if|if
condition|(
name|states
operator|.
name|get
argument_list|(
name|lowestIndex
argument_list|)
operator|.
name|next
argument_list|()
operator|==
name|DocsEnum
operator|.
name|NO_MORE_DOCS
condition|)
block|{
name|states
operator|.
name|remove
argument_list|(
name|lowestIndex
argument_list|)
expr_stmt|;
block|}
return|return
name|current
operator|=
name|lowestDocId
return|;
block|}
annotation|@
name|Override
DECL|method|advance
specifier|public
name|int
name|advance
parameter_list|(
name|int
name|target
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|cost
specifier|public
name|long
name|cost
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
DECL|class|State
class|class
name|State
block|{
DECL|field|docsEnum
specifier|final
name|DocsEnum
name|docsEnum
decl_stmt|;
DECL|field|current
name|int
name|current
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|State
name|State
parameter_list|(
name|DocsEnum
name|docsEnum
parameter_list|)
block|{
name|this
operator|.
name|docsEnum
operator|=
name|docsEnum
expr_stmt|;
block|}
DECL|method|initialize
name|void
name|initialize
parameter_list|()
throws|throws
name|IOException
block|{
name|current
operator|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
expr_stmt|;
block|}
DECL|method|next
name|int
name|next
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|current
operator|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
return|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|ord
specifier|public
name|long
name|ord
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|seekExact
specifier|public
name|void
name|seekExact
parameter_list|(
name|long
name|ord
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|docFreq
specifier|public
name|int
name|docFreq
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|totalTermFreq
specifier|public
name|long
name|totalTermFreq
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|docsAndPositions
specifier|public
name|DocsAndPositionsEnum
name|docsAndPositions
parameter_list|(
name|Bits
name|liveDocs
parameter_list|,
name|DocsAndPositionsEnum
name|reuse
parameter_list|,
name|int
name|flags
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
end_class

end_unit

