begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|MockScriptPlugin
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
name|lookup
operator|.
name|LeafDocLookup
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTime
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
import|;
end_import

begin_comment
comment|/**  * Mock scripts shared by DateRangeIT and DateHistogramIT.  *  * Provides {@link DateScriptMocksPlugin#EXTRACT_FIELD}, {@link DateScriptMocksPlugin#DOUBLE_PLUS_ONE_MONTH},  * and {@link DateScriptMocksPlugin#LONG_PLUS_ONE_MONTH} scripts.  */
end_comment

begin_class
DECL|class|DateScriptMocksPlugin
specifier|public
class|class
name|DateScriptMocksPlugin
extends|extends
name|MockScriptPlugin
block|{
DECL|field|EXTRACT_FIELD
specifier|static
specifier|final
name|String
name|EXTRACT_FIELD
init|=
literal|"extract_field"
decl_stmt|;
DECL|field|DOUBLE_PLUS_ONE_MONTH
specifier|static
specifier|final
name|String
name|DOUBLE_PLUS_ONE_MONTH
init|=
literal|"double_date_plus_1_month"
decl_stmt|;
DECL|field|LONG_PLUS_ONE_MONTH
specifier|static
specifier|final
name|String
name|LONG_PLUS_ONE_MONTH
init|=
literal|"long_date_plus_1_month"
decl_stmt|;
annotation|@
name|Override
DECL|method|pluginScripts
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Function
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|,
name|Object
argument_list|>
argument_list|>
name|pluginScripts
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Function
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|,
name|Object
argument_list|>
argument_list|>
name|scripts
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|scripts
operator|.
name|put
argument_list|(
name|EXTRACT_FIELD
argument_list|,
name|params
lambda|->
block|{
name|LeafDocLookup
name|docLookup
init|=
operator|(
name|LeafDocLookup
operator|)
name|params
operator|.
name|get
argument_list|(
literal|"doc"
argument_list|)
decl_stmt|;
name|String
name|fieldname
init|=
operator|(
name|String
operator|)
name|params
operator|.
name|get
argument_list|(
literal|"fieldname"
argument_list|)
decl_stmt|;
return|return
name|docLookup
operator|.
name|get
argument_list|(
name|fieldname
argument_list|)
return|;
block|}
argument_list|)
expr_stmt|;
name|scripts
operator|.
name|put
argument_list|(
name|DOUBLE_PLUS_ONE_MONTH
argument_list|,
name|params
lambda|->
operator|new
name|DateTime
argument_list|(
name|Double
operator|.
name|valueOf
argument_list|(
operator|(
name|double
operator|)
name|params
operator|.
name|get
argument_list|(
literal|"_value"
argument_list|)
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|plusMonths
argument_list|(
literal|1
argument_list|)
operator|.
name|getMillis
argument_list|()
argument_list|)
expr_stmt|;
name|scripts
operator|.
name|put
argument_list|(
name|LONG_PLUS_ONE_MONTH
argument_list|,
name|params
lambda|->
operator|new
name|DateTime
argument_list|(
operator|(
name|long
operator|)
name|params
operator|.
name|get
argument_list|(
literal|"_value"
argument_list|)
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|plusMonths
argument_list|(
literal|1
argument_list|)
operator|.
name|getMillis
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|scripts
return|;
block|}
block|}
end_class

end_unit
