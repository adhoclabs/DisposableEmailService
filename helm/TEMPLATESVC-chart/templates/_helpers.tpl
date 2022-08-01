{{/*
Generate configmap name suffix based on release number
*/}}
{{- define "TEMPLATE_SVC_BASE-chart.cm-suffix" -}}
{{- .Chart.Version | default "1.0.0" | replace "." "-" }}
{{- end }}

{{/*
Expand the name of the chart.
*/}}
{{- define "TEMPLATE_SVC_BASE-chart.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "TEMPLATE_SVC_BASE-chart.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "TEMPLATE_SVC_BASE-chart.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "TEMPLATE_SVC_BASE-chart.labels" -}}
helm.sh/chart: {{ include "TEMPLATE_SVC_BASE-chart.chart" . }}
{{ include "TEMPLATE_SVC_BASE-chart.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "TEMPLATE_SVC_BASE-chart.selectorLabels" -}}
app.kubernetes.io/name: {{ include "TEMPLATE_SVC_BASE-chart.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

