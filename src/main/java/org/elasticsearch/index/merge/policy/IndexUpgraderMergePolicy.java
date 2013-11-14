begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.merge.policy
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|merge
operator|.
name|policy
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
name|ImmutableList
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
name|index
operator|.
name|FieldInfo
operator|.
name|DocValuesType
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
name|FieldInfo
operator|.
name|IndexOptions
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
name|store
operator|.
name|Directory
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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|packed
operator|.
name|GrowableWriter
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
name|packed
operator|.
name|PackedInts
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
name|Numbers
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
name|mapper
operator|.
name|internal
operator|.
name|VersionFieldMapper
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
name|Collections
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * A {@link MergePolicy} that upgrades segments.  *<p>  * It can be useful to use the background merging process to upgrade segments,  * for example when we perform internal changes that imply different index  * options or when a user modifies his mapping in non-breaking ways: we could  * imagine using this merge policy to be able to add doc values to fields after  * the fact or on the opposite to remove them.  *<p>  * For now, this {@link MergePolicy} takes care of moving versions that used to  * be stored as payloads to numeric doc values.  */
end_comment

begin_class
DECL|class|IndexUpgraderMergePolicy
specifier|public
specifier|final
class|class
name|IndexUpgraderMergePolicy
extends|extends
name|MergePolicy
block|{
DECL|field|delegate
specifier|private
specifier|final
name|MergePolicy
name|delegate
decl_stmt|;
comment|/** @param delegate the merge policy to wrap */
DECL|method|IndexUpgraderMergePolicy
specifier|public
name|IndexUpgraderMergePolicy
parameter_list|(
name|MergePolicy
name|delegate
parameter_list|)
block|{
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
block|}
comment|/** Return an "upgraded" view of the reader. */
DECL|method|filter
specifier|static
name|AtomicReader
name|filter
parameter_list|(
name|AtomicReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|FieldInfos
name|fieldInfos
init|=
name|reader
operator|.
name|getFieldInfos
argument_list|()
decl_stmt|;
specifier|final
name|FieldInfo
name|versionInfo
init|=
name|fieldInfos
operator|.
name|fieldInfo
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|versionInfo
operator|!=
literal|null
operator|&&
name|versionInfo
operator|.
name|hasDocValues
argument_list|()
condition|)
block|{
comment|// the reader is a recent one, it has versions and they are stored
comment|// in a numeric doc values field
return|return
name|reader
return|;
block|}
comment|// The segment is an old one, load all versions in memory and hide
comment|// them behind a numeric doc values field
specifier|final
name|Terms
name|terms
init|=
name|reader
operator|.
name|terms
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|terms
operator|==
literal|null
operator|||
operator|!
name|terms
operator|.
name|hasPayloads
argument_list|()
condition|)
block|{
comment|// The segment doesn't have an _uid field or doesn't have paylods
comment|// don't try to do anything clever. If any other segment has versions
comment|// all versions of this segment will be initialized to 0
return|return
name|reader
return|;
block|}
specifier|final
name|TermsEnum
name|uids
init|=
name|terms
operator|.
name|iterator
argument_list|(
literal|null
argument_list|)
decl_stmt|;
specifier|final
name|GrowableWriter
name|versions
init|=
operator|new
name|GrowableWriter
argument_list|(
literal|2
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|PackedInts
operator|.
name|DEFAULT
argument_list|)
decl_stmt|;
name|DocsAndPositionsEnum
name|dpe
init|=
literal|null
decl_stmt|;
for|for
control|(
name|BytesRef
name|uid
init|=
name|uids
operator|.
name|next
argument_list|()
init|;
name|uid
operator|!=
literal|null
condition|;
name|uid
operator|=
name|uids
operator|.
name|next
argument_list|()
control|)
block|{
name|dpe
operator|=
name|uids
operator|.
name|docsAndPositions
argument_list|(
name|reader
operator|.
name|getLiveDocs
argument_list|()
argument_list|,
name|dpe
argument_list|,
name|DocsAndPositionsEnum
operator|.
name|FLAG_PAYLOADS
argument_list|)
expr_stmt|;
assert|assert
name|dpe
operator|!=
literal|null
operator|:
literal|"field has payloads"
assert|;
for|for
control|(
name|int
name|doc
init|=
name|dpe
operator|.
name|nextDoc
argument_list|()
init|;
name|doc
operator|!=
name|DocsEnum
operator|.
name|NO_MORE_DOCS
condition|;
name|doc
operator|=
name|dpe
operator|.
name|nextDoc
argument_list|()
control|)
block|{
name|dpe
operator|.
name|nextPosition
argument_list|()
expr_stmt|;
specifier|final
name|BytesRef
name|payload
init|=
name|dpe
operator|.
name|getPayload
argument_list|()
decl_stmt|;
if|if
condition|(
name|payload
operator|!=
literal|null
operator|&&
name|payload
operator|.
name|length
operator|==
literal|8
condition|)
block|{
specifier|final
name|long
name|version
init|=
name|Numbers
operator|.
name|bytesToLong
argument_list|(
name|payload
argument_list|)
decl_stmt|;
name|versions
operator|.
name|set
argument_list|(
name|doc
argument_list|,
name|version
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
comment|// Build new field infos, doc values, and return a filter reader
specifier|final
name|FieldInfo
name|newVersionInfo
decl_stmt|;
if|if
condition|(
name|versionInfo
operator|==
literal|null
condition|)
block|{
comment|// Find a free field number
name|int
name|fieldNumber
init|=
literal|0
decl_stmt|;
for|for
control|(
name|FieldInfo
name|fi
range|:
name|fieldInfos
control|)
block|{
name|fieldNumber
operator|=
name|Math
operator|.
name|max
argument_list|(
name|fieldNumber
argument_list|,
name|fi
operator|.
name|number
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|newVersionInfo
operator|=
operator|new
name|FieldInfo
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|,
literal|false
argument_list|,
name|fieldNumber
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
name|IndexOptions
operator|.
name|DOCS_ONLY
argument_list|,
name|DocValuesType
operator|.
name|NUMERIC
argument_list|,
name|DocValuesType
operator|.
name|NUMERIC
argument_list|,
name|Collections
operator|.
expr|<
name|String
argument_list|,
name|String
operator|>
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newVersionInfo
operator|=
operator|new
name|FieldInfo
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|,
name|versionInfo
operator|.
name|isIndexed
argument_list|()
argument_list|,
name|versionInfo
operator|.
name|number
argument_list|,
name|versionInfo
operator|.
name|hasVectors
argument_list|()
argument_list|,
name|versionInfo
operator|.
name|omitsNorms
argument_list|()
argument_list|,
name|versionInfo
operator|.
name|hasPayloads
argument_list|()
argument_list|,
name|versionInfo
operator|.
name|getIndexOptions
argument_list|()
argument_list|,
name|versionInfo
operator|.
name|getDocValuesType
argument_list|()
argument_list|,
name|versionInfo
operator|.
name|getNormType
argument_list|()
argument_list|,
name|versionInfo
operator|.
name|attributes
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|final
name|ArrayList
argument_list|<
name|FieldInfo
argument_list|>
name|fieldInfoList
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldInfo
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldInfo
name|info
range|:
name|fieldInfos
control|)
block|{
if|if
condition|(
name|info
operator|!=
name|versionInfo
condition|)
block|{
name|fieldInfoList
operator|.
name|add
argument_list|(
name|info
argument_list|)
expr_stmt|;
block|}
block|}
name|fieldInfoList
operator|.
name|add
argument_list|(
name|newVersionInfo
argument_list|)
expr_stmt|;
specifier|final
name|FieldInfos
name|newFieldInfos
init|=
operator|new
name|FieldInfos
argument_list|(
name|fieldInfoList
operator|.
name|toArray
argument_list|(
operator|new
name|FieldInfo
index|[
name|fieldInfoList
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|NumericDocValues
name|versionValues
init|=
operator|new
name|NumericDocValues
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|versions
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
block|}
decl_stmt|;
return|return
operator|new
name|FilterAtomicReader
argument_list|(
name|reader
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|FieldInfos
name|getFieldInfos
parameter_list|()
block|{
return|return
name|newFieldInfos
return|;
block|}
annotation|@
name|Override
specifier|public
name|NumericDocValues
name|getNumericDocValues
parameter_list|(
name|String
name|field
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|VersionFieldMapper
operator|.
name|NAME
operator|.
name|equals
argument_list|(
name|field
argument_list|)
condition|)
block|{
return|return
name|versionValues
return|;
block|}
return|return
name|super
operator|.
name|getNumericDocValues
argument_list|(
name|field
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Bits
name|getDocsWithField
parameter_list|(
name|String
name|field
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|Bits
operator|.
name|MatchAllBits
argument_list|(
name|in
operator|.
name|maxDoc
argument_list|()
argument_list|)
return|;
block|}
block|}
return|;
block|}
DECL|class|IndexUpgraderOneMerge
specifier|static
class|class
name|IndexUpgraderOneMerge
extends|extends
name|OneMerge
block|{
DECL|method|IndexUpgraderOneMerge
specifier|public
name|IndexUpgraderOneMerge
parameter_list|(
name|List
argument_list|<
name|SegmentCommitInfo
argument_list|>
name|segments
parameter_list|)
block|{
name|super
argument_list|(
name|segments
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getMergeReaders
specifier|public
name|List
argument_list|<
name|AtomicReader
argument_list|>
name|getMergeReaders
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|List
argument_list|<
name|AtomicReader
argument_list|>
name|readers
init|=
name|super
operator|.
name|getMergeReaders
argument_list|()
decl_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|AtomicReader
argument_list|>
name|newReaders
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|AtomicReader
name|reader
range|:
name|readers
control|)
block|{
name|newReaders
operator|.
name|add
argument_list|(
name|filter
argument_list|(
name|reader
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|newReaders
operator|.
name|build
argument_list|()
return|;
block|}
block|}
DECL|class|IndexUpgraderMergeSpecification
specifier|static
class|class
name|IndexUpgraderMergeSpecification
extends|extends
name|MergeSpecification
block|{
annotation|@
name|Override
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|OneMerge
name|merge
parameter_list|)
block|{
name|super
operator|.
name|add
argument_list|(
operator|new
name|IndexUpgraderOneMerge
argument_list|(
name|merge
operator|.
name|segments
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|segString
specifier|public
name|String
name|segString
parameter_list|(
name|Directory
name|dir
parameter_list|)
block|{
return|return
literal|"IndexUpgraderMergeSpec["
operator|+
name|super
operator|.
name|segString
argument_list|(
name|dir
argument_list|)
operator|+
literal|"]"
return|;
block|}
block|}
DECL|method|upgradedMergeSpecification
specifier|static
name|MergeSpecification
name|upgradedMergeSpecification
parameter_list|(
name|MergeSpecification
name|spec
parameter_list|)
block|{
if|if
condition|(
name|spec
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|MergeSpecification
name|upgradedSpec
init|=
operator|new
name|IndexUpgraderMergeSpecification
argument_list|()
decl_stmt|;
for|for
control|(
name|OneMerge
name|merge
range|:
name|spec
operator|.
name|merges
control|)
block|{
name|upgradedSpec
operator|.
name|add
argument_list|(
name|merge
argument_list|)
expr_stmt|;
block|}
return|return
name|upgradedSpec
return|;
block|}
annotation|@
name|Override
DECL|method|findMerges
specifier|public
name|MergeSpecification
name|findMerges
parameter_list|(
name|MergeTrigger
name|mergeTrigger
parameter_list|,
name|SegmentInfos
name|segmentInfos
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|upgradedMergeSpecification
argument_list|(
name|delegate
operator|.
name|findMerges
argument_list|(
name|mergeTrigger
argument_list|,
name|segmentInfos
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|findForcedMerges
specifier|public
name|MergeSpecification
name|findForcedMerges
parameter_list|(
name|SegmentInfos
name|segmentInfos
parameter_list|,
name|int
name|maxSegmentCount
parameter_list|,
name|Map
argument_list|<
name|SegmentCommitInfo
argument_list|,
name|Boolean
argument_list|>
name|segmentsToMerge
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|upgradedMergeSpecification
argument_list|(
name|delegate
operator|.
name|findForcedMerges
argument_list|(
name|segmentInfos
argument_list|,
name|maxSegmentCount
argument_list|,
name|segmentsToMerge
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|findForcedDeletesMerges
specifier|public
name|MergeSpecification
name|findForcedDeletesMerges
parameter_list|(
name|SegmentInfos
name|segmentInfos
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|upgradedMergeSpecification
argument_list|(
name|delegate
operator|.
name|findForcedDeletesMerges
argument_list|(
name|segmentInfos
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|clone
specifier|public
name|MergePolicy
name|clone
parameter_list|()
block|{
return|return
operator|new
name|IndexUpgraderMergePolicy
argument_list|(
name|delegate
operator|.
name|clone
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|delegate
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|useCompoundFile
specifier|public
name|boolean
name|useCompoundFile
parameter_list|(
name|SegmentInfos
name|segments
parameter_list|,
name|SegmentCommitInfo
name|newSegment
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|useCompoundFile
argument_list|(
name|segments
argument_list|,
name|newSegment
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|setIndexWriter
specifier|public
name|void
name|setIndexWriter
parameter_list|(
name|IndexWriter
name|writer
parameter_list|)
block|{
name|delegate
operator|.
name|setIndexWriter
argument_list|(
name|writer
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"("
operator|+
name|delegate
operator|+
literal|")"
return|;
block|}
block|}
end_class

end_unit

