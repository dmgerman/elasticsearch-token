begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facets.histogram
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facets
operator|.
name|histogram
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
name|IndexReader
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
name|field
operator|.
name|data
operator|.
name|FieldDataCache
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
name|field
operator|.
name|data
operator|.
name|FieldData
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
name|field
operator|.
name|data
operator|.
name|NumericFieldData
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
name|FieldMapper
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
name|MapperService
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
name|facets
operator|.
name|Facet
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
name|facets
operator|.
name|FacetPhaseExecutionException
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
name|facets
operator|.
name|support
operator|.
name|AbstractFacetCollector
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
name|gnu
operator|.
name|trove
operator|.
name|TLongDoubleHashMap
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
name|gnu
operator|.
name|trove
operator|.
name|TLongLongHashMap
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
name|elasticsearch
operator|.
name|index
operator|.
name|field
operator|.
name|data
operator|.
name|FieldDataOptions
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * A histogram facet collector that uses different fields for the key and the value.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|KeyValueHistogramFacetCollector
specifier|public
class|class
name|KeyValueHistogramFacetCollector
extends|extends
name|AbstractFacetCollector
block|{
DECL|field|keyFieldName
specifier|private
specifier|final
name|String
name|keyFieldName
decl_stmt|;
DECL|field|keyIndexFieldName
specifier|private
specifier|final
name|String
name|keyIndexFieldName
decl_stmt|;
DECL|field|valueFieldName
specifier|private
specifier|final
name|String
name|valueFieldName
decl_stmt|;
DECL|field|valueIndexFieldName
specifier|private
specifier|final
name|String
name|valueIndexFieldName
decl_stmt|;
DECL|field|interval
specifier|private
specifier|final
name|long
name|interval
decl_stmt|;
DECL|field|comparatorType
specifier|private
specifier|final
name|HistogramFacet
operator|.
name|ComparatorType
name|comparatorType
decl_stmt|;
DECL|field|fieldDataCache
specifier|private
specifier|final
name|FieldDataCache
name|fieldDataCache
decl_stmt|;
DECL|field|keyFieldDataType
specifier|private
specifier|final
name|FieldData
operator|.
name|Type
name|keyFieldDataType
decl_stmt|;
DECL|field|keyFieldData
specifier|private
name|NumericFieldData
name|keyFieldData
decl_stmt|;
DECL|field|valueFieldDataType
specifier|private
specifier|final
name|FieldData
operator|.
name|Type
name|valueFieldDataType
decl_stmt|;
DECL|field|valueFieldData
specifier|private
name|NumericFieldData
name|valueFieldData
decl_stmt|;
DECL|field|counts
specifier|private
specifier|final
name|TLongLongHashMap
name|counts
init|=
operator|new
name|TLongLongHashMap
argument_list|()
decl_stmt|;
DECL|field|totals
specifier|private
specifier|final
name|TLongDoubleHashMap
name|totals
init|=
operator|new
name|TLongDoubleHashMap
argument_list|()
decl_stmt|;
DECL|method|KeyValueHistogramFacetCollector
specifier|public
name|KeyValueHistogramFacetCollector
parameter_list|(
name|String
name|facetName
parameter_list|,
name|String
name|keyFieldName
parameter_list|,
name|String
name|valueFieldName
parameter_list|,
name|long
name|interval
parameter_list|,
name|HistogramFacet
operator|.
name|ComparatorType
name|comparatorType
parameter_list|,
name|FieldDataCache
name|fieldDataCache
parameter_list|,
name|MapperService
name|mapperService
parameter_list|)
block|{
name|super
argument_list|(
name|facetName
argument_list|)
expr_stmt|;
name|this
operator|.
name|keyFieldName
operator|=
name|keyFieldName
expr_stmt|;
name|this
operator|.
name|valueFieldName
operator|=
name|valueFieldName
expr_stmt|;
name|this
operator|.
name|interval
operator|=
name|interval
expr_stmt|;
name|this
operator|.
name|comparatorType
operator|=
name|comparatorType
expr_stmt|;
name|this
operator|.
name|fieldDataCache
operator|=
name|fieldDataCache
expr_stmt|;
name|FieldMapper
name|mapper
init|=
name|mapperService
operator|.
name|smartNameFieldMapper
argument_list|(
name|keyFieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|FacetPhaseExecutionException
argument_list|(
name|facetName
argument_list|,
literal|"No mapping found for key_field ["
operator|+
name|keyFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|keyIndexFieldName
operator|=
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
expr_stmt|;
name|keyFieldDataType
operator|=
name|mapper
operator|.
name|fieldDataType
argument_list|()
expr_stmt|;
name|mapper
operator|=
name|mapperService
operator|.
name|smartNameFieldMapper
argument_list|(
name|valueFieldName
argument_list|)
expr_stmt|;
if|if
condition|(
name|mapper
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|FacetPhaseExecutionException
argument_list|(
name|facetName
argument_list|,
literal|"No mapping found for value_field ["
operator|+
name|valueFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|valueIndexFieldName
operator|=
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
expr_stmt|;
name|valueFieldDataType
operator|=
name|mapper
operator|.
name|fieldDataType
argument_list|()
expr_stmt|;
block|}
DECL|method|doCollect
annotation|@
name|Override
specifier|protected
name|void
name|doCollect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|keyFieldData
operator|.
name|multiValued
argument_list|()
condition|)
block|{
if|if
condition|(
name|valueFieldData
operator|.
name|multiValued
argument_list|()
condition|)
block|{
comment|// both multi valued, intersect based on the minimum size
name|double
index|[]
name|keys
init|=
name|keyFieldData
operator|.
name|doubleValues
argument_list|(
name|doc
argument_list|)
decl_stmt|;
name|double
index|[]
name|values
init|=
name|valueFieldData
operator|.
name|doubleValues
argument_list|(
name|doc
argument_list|)
decl_stmt|;
name|int
name|size
init|=
name|Math
operator|.
name|min
argument_list|(
name|keys
operator|.
name|length
argument_list|,
name|values
operator|.
name|length
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|long
name|bucket
init|=
name|HistogramFacetCollector
operator|.
name|bucket
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|,
name|interval
argument_list|)
decl_stmt|;
name|counts
operator|.
name|adjustOrPutValue
argument_list|(
name|bucket
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|totals
operator|.
name|adjustOrPutValue
argument_list|(
name|bucket
argument_list|,
name|values
index|[
name|i
index|]
argument_list|,
name|values
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// key multi valued, value is a single value
name|double
name|value
init|=
name|valueFieldData
operator|.
name|doubleValue
argument_list|(
name|doc
argument_list|)
decl_stmt|;
for|for
control|(
name|double
name|key
range|:
name|keyFieldData
operator|.
name|doubleValues
argument_list|(
name|doc
argument_list|)
control|)
block|{
name|long
name|bucket
init|=
name|HistogramFacetCollector
operator|.
name|bucket
argument_list|(
name|key
argument_list|,
name|interval
argument_list|)
decl_stmt|;
name|counts
operator|.
name|adjustOrPutValue
argument_list|(
name|bucket
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|totals
operator|.
name|adjustOrPutValue
argument_list|(
name|bucket
argument_list|,
name|value
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// single key value, compute the bucket once
name|long
name|bucket
init|=
name|HistogramFacetCollector
operator|.
name|bucket
argument_list|(
name|keyFieldData
operator|.
name|doubleValue
argument_list|(
name|doc
argument_list|)
argument_list|,
name|interval
argument_list|)
decl_stmt|;
if|if
condition|(
name|valueFieldData
operator|.
name|multiValued
argument_list|()
condition|)
block|{
for|for
control|(
name|double
name|value
range|:
name|valueFieldData
operator|.
name|doubleValues
argument_list|(
name|doc
argument_list|)
control|)
block|{
name|counts
operator|.
name|adjustOrPutValue
argument_list|(
name|bucket
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|totals
operator|.
name|adjustOrPutValue
argument_list|(
name|bucket
argument_list|,
name|value
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// both key and value are not multi valued
name|double
name|value
init|=
name|valueFieldData
operator|.
name|doubleValue
argument_list|(
name|doc
argument_list|)
decl_stmt|;
name|counts
operator|.
name|adjustOrPutValue
argument_list|(
name|bucket
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|totals
operator|.
name|adjustOrPutValue
argument_list|(
name|bucket
argument_list|,
name|value
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|doSetNextReader
annotation|@
name|Override
specifier|protected
name|void
name|doSetNextReader
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|int
name|docBase
parameter_list|)
throws|throws
name|IOException
block|{
name|keyFieldData
operator|=
operator|(
name|NumericFieldData
operator|)
name|fieldDataCache
operator|.
name|cache
argument_list|(
name|keyFieldDataType
argument_list|,
name|reader
argument_list|,
name|keyIndexFieldName
argument_list|,
name|fieldDataOptions
argument_list|()
operator|.
name|withFreqs
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|valueFieldData
operator|=
operator|(
name|NumericFieldData
operator|)
name|fieldDataCache
operator|.
name|cache
argument_list|(
name|valueFieldDataType
argument_list|,
name|reader
argument_list|,
name|valueIndexFieldName
argument_list|,
name|fieldDataOptions
argument_list|()
operator|.
name|withFreqs
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|facet
annotation|@
name|Override
specifier|public
name|Facet
name|facet
parameter_list|()
block|{
return|return
operator|new
name|InternalHistogramFacet
argument_list|(
name|facetName
argument_list|,
name|keyFieldName
argument_list|,
name|valueFieldName
argument_list|,
name|interval
argument_list|,
name|comparatorType
argument_list|,
name|counts
argument_list|,
name|totals
argument_list|)
return|;
block|}
block|}
end_class

end_unit

