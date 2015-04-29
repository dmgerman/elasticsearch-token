begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script.expression
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|expression
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Calendar
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TimeZone
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
name|queries
operator|.
name|function
operator|.
name|ValueSource
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
name|AtomicNumericFieldData
import|;
end_import

begin_class
DECL|class|DateMethodFunctionValues
class|class
name|DateMethodFunctionValues
extends|extends
name|FieldDataFunctionValues
block|{
DECL|field|calendarType
specifier|private
specifier|final
name|int
name|calendarType
decl_stmt|;
DECL|field|calendar
specifier|private
specifier|final
name|Calendar
name|calendar
decl_stmt|;
DECL|method|DateMethodFunctionValues
name|DateMethodFunctionValues
parameter_list|(
name|ValueSource
name|parent
parameter_list|,
name|AtomicNumericFieldData
name|data
parameter_list|,
name|int
name|calendarType
parameter_list|)
block|{
name|super
argument_list|(
name|parent
argument_list|,
name|data
argument_list|)
expr_stmt|;
name|this
operator|.
name|calendarType
operator|=
name|calendarType
expr_stmt|;
name|calendar
operator|=
name|Calendar
operator|.
name|getInstance
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"GMT"
argument_list|)
argument_list|,
name|Locale
operator|.
name|ROOT
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doubleVal
specifier|public
name|double
name|doubleVal
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|long
name|millis
init|=
operator|(
name|long
operator|)
name|dataAccessor
operator|.
name|get
argument_list|(
name|docId
argument_list|)
decl_stmt|;
name|calendar
operator|.
name|setTimeInMillis
argument_list|(
name|millis
argument_list|)
expr_stmt|;
return|return
name|calendar
operator|.
name|get
argument_list|(
name|calendarType
argument_list|)
return|;
block|}
block|}
end_class

end_unit

