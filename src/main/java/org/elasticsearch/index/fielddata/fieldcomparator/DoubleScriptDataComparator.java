begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata.fieldcomparator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|fieldcomparator
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
name|AtomicReaderContext
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
name|FieldComparator
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
name|Scorer
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
name|SortField
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
name|fielddata
operator|.
name|IndexFieldData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|SearchScript
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
comment|/**  *  */
end_comment

begin_comment
comment|// LUCENE MONITOR: Monitor against FieldComparator.Double
end_comment

begin_class
DECL|class|DoubleScriptDataComparator
specifier|public
class|class
name|DoubleScriptDataComparator
extends|extends
name|NumberComparatorBase
argument_list|<
name|Double
argument_list|>
block|{
DECL|method|comparatorSource
specifier|public
specifier|static
name|IndexFieldData
operator|.
name|XFieldComparatorSource
name|comparatorSource
parameter_list|(
name|SearchScript
name|script
parameter_list|)
block|{
return|return
operator|new
name|InnerSource
argument_list|(
name|script
argument_list|)
return|;
block|}
DECL|class|InnerSource
specifier|private
specifier|static
class|class
name|InnerSource
extends|extends
name|IndexFieldData
operator|.
name|XFieldComparatorSource
block|{
DECL|field|script
specifier|private
specifier|final
name|SearchScript
name|script
decl_stmt|;
DECL|method|InnerSource
specifier|private
name|InnerSource
parameter_list|(
name|SearchScript
name|script
parameter_list|)
block|{
name|this
operator|.
name|script
operator|=
name|script
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newComparator
specifier|public
name|FieldComparator
argument_list|<
name|?
extends|extends
name|Number
argument_list|>
name|newComparator
parameter_list|(
name|String
name|fieldname
parameter_list|,
name|int
name|numHits
parameter_list|,
name|int
name|sortPos
parameter_list|,
name|boolean
name|reversed
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|DoubleScriptDataComparator
argument_list|(
name|numHits
argument_list|,
name|script
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|reducedType
specifier|public
name|SortField
operator|.
name|Type
name|reducedType
parameter_list|()
block|{
return|return
name|SortField
operator|.
name|Type
operator|.
name|DOUBLE
return|;
block|}
block|}
DECL|field|script
specifier|private
specifier|final
name|SearchScript
name|script
decl_stmt|;
DECL|field|values
specifier|private
specifier|final
name|double
index|[]
name|values
decl_stmt|;
DECL|field|bottom
specifier|private
name|double
name|bottom
decl_stmt|;
DECL|method|DoubleScriptDataComparator
specifier|public
name|DoubleScriptDataComparator
parameter_list|(
name|int
name|numHits
parameter_list|,
name|SearchScript
name|script
parameter_list|)
block|{
name|this
operator|.
name|script
operator|=
name|script
expr_stmt|;
name|values
operator|=
operator|new
name|double
index|[
name|numHits
index|]
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|FieldComparator
argument_list|<
name|Double
argument_list|>
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|script
operator|.
name|setNextReader
argument_list|(
name|context
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|setScorer
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
block|{
name|script
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|int
name|slot1
parameter_list|,
name|int
name|slot2
parameter_list|)
block|{
specifier|final
name|double
name|v1
init|=
name|values
index|[
name|slot1
index|]
decl_stmt|;
specifier|final
name|double
name|v2
init|=
name|values
index|[
name|slot2
index|]
decl_stmt|;
if|if
condition|(
name|v1
operator|>
name|v2
condition|)
block|{
return|return
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|v1
operator|<
name|v2
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|compareBottom
specifier|public
name|int
name|compareBottom
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
name|script
operator|.
name|setNextDocId
argument_list|(
name|doc
argument_list|)
expr_stmt|;
specifier|final
name|double
name|v2
init|=
name|script
operator|.
name|runAsDouble
argument_list|()
decl_stmt|;
if|if
condition|(
name|bottom
operator|>
name|v2
condition|)
block|{
return|return
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|bottom
operator|<
name|v2
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|compareDocToValue
specifier|public
name|int
name|compareDocToValue
parameter_list|(
name|int
name|doc
parameter_list|,
name|Double
name|val2
parameter_list|)
throws|throws
name|IOException
block|{
name|script
operator|.
name|setNextDocId
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|double
name|val1
init|=
name|script
operator|.
name|runAsDouble
argument_list|()
decl_stmt|;
return|return
name|Double
operator|.
name|compare
argument_list|(
name|val1
argument_list|,
name|val2
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|copy
specifier|public
name|void
name|copy
parameter_list|(
name|int
name|slot
parameter_list|,
name|int
name|doc
parameter_list|)
block|{
name|script
operator|.
name|setNextDocId
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|values
index|[
name|slot
index|]
operator|=
name|script
operator|.
name|runAsDouble
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setBottom
specifier|public
name|void
name|setBottom
parameter_list|(
specifier|final
name|int
name|bottom
parameter_list|)
block|{
name|this
operator|.
name|bottom
operator|=
name|values
index|[
name|bottom
index|]
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|value
specifier|public
name|Double
name|value
parameter_list|(
name|int
name|slot
parameter_list|)
block|{
return|return
name|values
index|[
name|slot
index|]
return|;
block|}
annotation|@
name|Override
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|int
name|slot
parameter_list|,
name|int
name|doc
parameter_list|)
block|{
name|script
operator|.
name|setNextDocId
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|values
index|[
name|slot
index|]
operator|+=
name|script
operator|.
name|runAsDouble
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|divide
specifier|public
name|void
name|divide
parameter_list|(
name|int
name|slot
parameter_list|,
name|int
name|divisor
parameter_list|)
block|{
name|values
index|[
name|slot
index|]
operator|/=
name|divisor
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|missing
specifier|public
name|void
name|missing
parameter_list|(
name|int
name|slot
parameter_list|)
block|{
name|values
index|[
name|slot
index|]
operator|=
name|Double
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|compareBottomMissing
specifier|public
name|int
name|compareBottomMissing
parameter_list|()
block|{
return|return
name|Double
operator|.
name|compare
argument_list|(
name|bottom
argument_list|,
name|Double
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
block|}
end_class

end_unit

