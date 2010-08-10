begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facets.range
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facets
operator|.
name|range
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
name|search
operator|.
name|internal
operator|.
name|SearchContext
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|KeyValueRangeFacetCollector
specifier|public
class|class
name|KeyValueRangeFacetCollector
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
DECL|field|entries
specifier|private
specifier|final
name|RangeFacet
operator|.
name|Entry
index|[]
name|entries
decl_stmt|;
DECL|method|KeyValueRangeFacetCollector
specifier|public
name|KeyValueRangeFacetCollector
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
name|RangeFacet
operator|.
name|Entry
index|[]
name|entries
parameter_list|,
name|SearchContext
name|context
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
name|entries
operator|=
name|entries
expr_stmt|;
name|this
operator|.
name|fieldDataCache
operator|=
name|context
operator|.
name|fieldDataCache
argument_list|()
expr_stmt|;
name|MapperService
operator|.
name|SmartNameFieldMappers
name|smartMappers
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|smartName
argument_list|(
name|keyFieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|smartMappers
operator|==
literal|null
operator|||
operator|!
name|smartMappers
operator|.
name|hasMapper
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|FacetPhaseExecutionException
argument_list|(
name|facetName
argument_list|,
literal|"No mapping found for field ["
operator|+
name|keyFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|// add type filter if there is exact doc mapper associated with it
if|if
condition|(
name|smartMappers
operator|.
name|hasDocMapper
argument_list|()
condition|)
block|{
name|setFilter
argument_list|(
name|context
operator|.
name|filterCache
argument_list|()
operator|.
name|cache
argument_list|(
name|smartMappers
operator|.
name|docMapper
argument_list|()
operator|.
name|typeFilter
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|keyIndexFieldName
operator|=
name|smartMappers
operator|.
name|mapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
expr_stmt|;
name|keyFieldDataType
operator|=
name|smartMappers
operator|.
name|mapper
argument_list|()
operator|.
name|fieldDataType
argument_list|()
expr_stmt|;
name|FieldMapper
name|mapper
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
name|valueFieldName
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
argument_list|)
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
name|double
name|key
init|=
name|keys
index|[
name|i
index|]
decl_stmt|;
for|for
control|(
name|RangeFacet
operator|.
name|Entry
name|entry
range|:
name|entries
control|)
block|{
if|if
condition|(
name|key
operator|>=
name|entry
operator|.
name|getFrom
argument_list|()
operator|&&
name|key
operator|<
name|entry
operator|.
name|getTo
argument_list|()
condition|)
block|{
name|entry
operator|.
name|count
operator|++
expr_stmt|;
name|entry
operator|.
name|total
operator|+=
name|values
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
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
for|for
control|(
name|RangeFacet
operator|.
name|Entry
name|entry
range|:
name|entries
control|)
block|{
if|if
condition|(
name|key
operator|>=
name|entry
operator|.
name|getFrom
argument_list|()
operator|&&
name|key
operator|<
name|entry
operator|.
name|getTo
argument_list|()
condition|)
block|{
name|entry
operator|.
name|count
operator|++
expr_stmt|;
name|entry
operator|.
name|total
operator|+=
name|value
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
else|else
block|{
name|double
name|key
init|=
name|keyFieldData
operator|.
name|doubleValue
argument_list|(
name|doc
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
name|RangeFacet
operator|.
name|Entry
name|entry
range|:
name|entries
control|)
block|{
if|if
condition|(
name|key
operator|>=
name|entry
operator|.
name|getFrom
argument_list|()
operator|&&
name|key
operator|<
name|entry
operator|.
name|getTo
argument_list|()
condition|)
block|{
name|entry
operator|.
name|count
operator|++
expr_stmt|;
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
name|entry
operator|.
name|total
operator|+=
name|value
expr_stmt|;
block|}
block|}
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
for|for
control|(
name|RangeFacet
operator|.
name|Entry
name|entry
range|:
name|entries
control|)
block|{
if|if
condition|(
name|key
operator|>=
name|entry
operator|.
name|getFrom
argument_list|()
operator|&&
name|key
operator|<
name|entry
operator|.
name|getTo
argument_list|()
condition|)
block|{
name|entry
operator|.
name|count
operator|++
expr_stmt|;
name|entry
operator|.
name|total
operator|+=
name|value
expr_stmt|;
block|}
block|}
block|}
block|}
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
name|InternalRangeDistanceFacet
argument_list|(
name|facetName
argument_list|,
name|keyFieldName
argument_list|,
name|valueFieldName
argument_list|,
name|entries
argument_list|)
return|;
block|}
block|}
end_class

end_unit

