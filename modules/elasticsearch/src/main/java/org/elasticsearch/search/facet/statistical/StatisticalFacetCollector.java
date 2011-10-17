begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.statistical
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|statistical
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
name|FieldDataType
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
name|facet
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
name|facet
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
name|facet
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
DECL|class|StatisticalFacetCollector
specifier|public
class|class
name|StatisticalFacetCollector
extends|extends
name|AbstractFacetCollector
block|{
DECL|field|indexFieldName
specifier|private
specifier|final
name|String
name|indexFieldName
decl_stmt|;
DECL|field|fieldDataCache
specifier|private
specifier|final
name|FieldDataCache
name|fieldDataCache
decl_stmt|;
DECL|field|fieldDataType
specifier|private
specifier|final
name|FieldDataType
name|fieldDataType
decl_stmt|;
DECL|field|fieldData
specifier|private
name|NumericFieldData
name|fieldData
decl_stmt|;
DECL|field|statsProc
specifier|private
specifier|final
name|StatsProc
name|statsProc
init|=
operator|new
name|StatsProc
argument_list|()
decl_stmt|;
DECL|method|StatisticalFacetCollector
specifier|public
name|StatisticalFacetCollector
parameter_list|(
name|String
name|facetName
parameter_list|,
name|String
name|fieldName
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
name|smartFieldMappers
argument_list|(
name|fieldName
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
name|fieldName
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
operator|&&
name|smartMappers
operator|.
name|explicitTypeInName
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
name|indexFieldName
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
name|fieldDataType
operator|=
name|smartMappers
operator|.
name|mapper
argument_list|()
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
name|fieldData
operator|.
name|forEachValueInDoc
argument_list|(
name|doc
argument_list|,
name|statsProc
argument_list|)
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
name|fieldData
operator|=
operator|(
name|NumericFieldData
operator|)
name|fieldDataCache
operator|.
name|cache
argument_list|(
name|fieldDataType
argument_list|,
name|reader
argument_list|,
name|indexFieldName
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
name|InternalStatisticalFacet
argument_list|(
name|facetName
argument_list|,
name|statsProc
operator|.
name|min
argument_list|()
argument_list|,
name|statsProc
operator|.
name|max
argument_list|()
argument_list|,
name|statsProc
operator|.
name|total
argument_list|()
argument_list|,
name|statsProc
operator|.
name|sumOfSquares
argument_list|()
argument_list|,
name|statsProc
operator|.
name|count
argument_list|()
argument_list|)
return|;
block|}
DECL|class|StatsProc
specifier|public
specifier|static
class|class
name|StatsProc
implements|implements
name|NumericFieldData
operator|.
name|MissingDoubleValueInDocProc
block|{
DECL|field|min
name|double
name|min
init|=
name|Double
operator|.
name|POSITIVE_INFINITY
decl_stmt|;
DECL|field|max
name|double
name|max
init|=
name|Double
operator|.
name|NEGATIVE_INFINITY
decl_stmt|;
DECL|field|total
name|double
name|total
init|=
literal|0
decl_stmt|;
DECL|field|sumOfSquares
name|double
name|sumOfSquares
init|=
literal|0.0
decl_stmt|;
DECL|field|count
name|long
name|count
decl_stmt|;
DECL|field|missing
name|int
name|missing
decl_stmt|;
DECL|method|onValue
annotation|@
name|Override
specifier|public
name|void
name|onValue
parameter_list|(
name|int
name|docId
parameter_list|,
name|double
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|<
name|min
condition|)
block|{
name|min
operator|=
name|value
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|>
name|max
condition|)
block|{
name|max
operator|=
name|value
expr_stmt|;
block|}
name|sumOfSquares
operator|+=
name|value
operator|*
name|value
expr_stmt|;
name|total
operator|+=
name|value
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
DECL|method|onMissing
annotation|@
name|Override
specifier|public
name|void
name|onMissing
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|missing
operator|++
expr_stmt|;
block|}
DECL|method|min
specifier|public
specifier|final
name|double
name|min
parameter_list|()
block|{
return|return
name|min
return|;
block|}
DECL|method|max
specifier|public
specifier|final
name|double
name|max
parameter_list|()
block|{
return|return
name|max
return|;
block|}
DECL|method|total
specifier|public
specifier|final
name|double
name|total
parameter_list|()
block|{
return|return
name|total
return|;
block|}
DECL|method|count
specifier|public
specifier|final
name|long
name|count
parameter_list|()
block|{
return|return
name|count
return|;
block|}
DECL|method|sumOfSquares
specifier|public
specifier|final
name|double
name|sumOfSquares
parameter_list|()
block|{
return|return
name|sumOfSquares
return|;
block|}
block|}
block|}
end_class

end_unit

